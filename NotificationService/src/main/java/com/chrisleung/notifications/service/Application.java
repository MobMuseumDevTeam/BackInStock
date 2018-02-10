package com.chrisleung.notifications.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.chrisleung.notifications.objects.Notification;
import com.chrisleung.notifications.objects.NotificationWrapper;
import com.shopify.api.*;

@SpringBootApplication
public class Application {

    private ApplicationProperties appProperties;
    private BlockingQueue<EmailNotification> emailQueue;    
    private Log logger;
    
	public static void main(String args[]) {
		SpringApplication.run(Application.class);
	}

	@Autowired
	public void setApp(ApplicationProperties ap) {
	    this.appProperties = ap;
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
		    /* 0. General Setup  */
		    logger = new Log(appProperties.getLog());
		    emailQueue = new LinkedBlockingQueue<>(appProperties.getEmail().getLimits().getQueueSize());
		    
		    /* 1. API Setup */
            NotificationsApi notificationsApi = new NotificationsApi(restTemplate, appProperties.getRestapi()); 
       	    ShopifyApi shopifyApi= new ShopifyApi(restTemplate, appProperties.getShopifyapi());
       	    EmailService emailService = new EmailService(appProperties.getEmail(),emailQueue,notificationsApi,logger);
       	    emailService.start();

        	    /* 2. Retrieve unsent notifications from the Stock Notifications REST API */
       	    NotificationWrapper notificationResponse = notificationsApi.getAllUnsentNotifications(); 
			Iterable<Notification> newNotifications = notificationResponse.getNotifications();
			Date lastUpdate = notificationResponse.getCurrentDate();
			Set<String> allNotifications = new HashSet<>(); // Used to detect duplicates when updating
			for(Notification n : newNotifications) {
			    allNotifications.add(n.getId());
			}
			
			/* Program Loop Setup */
			long sleepMs = appProperties.getRestapi().getRefresh() * 1000;
            // The main data structure: variant-ID to notifications map
            Map<Integer,List<Notification>> variantNotificationMap = new HashMap<Integer,List<Notification>>();
            int totalQueued = 0; // For log output
            
            /* Program Loop */
            logger.message("Starting Notification Service...");

            while(true) {

                /* 3. Add new notifications to the variant ID-notification map */
                int numNew = 0; // For current iteration's log output
			    for(Notification n : newNotifications) {
    			        List<Notification> l = variantNotificationMap.get(n.getVariantId());
    			        if(l == null) {
    			            l = new ArrayList<>();
    			            variantNotificationMap.put(n.getVariantId(), l);
    			        }
    			        l.add(n);
                    numNew++;
			    }

			    /* 4. Detect variants that are back in stock */
                int numOutOfStock = 0; // For current iteration's log output
                List<Variant> inStock = new ArrayList<>();
                Map<Variant,Product> variantProductMap = new HashMap<>(); // Variant product data
			    for(Integer variantId : variantNotificationMap.keySet()) {
			        Variant v = shopifyApi.getVariant(variantId);
                    if(v.getInventory_quantity() > 0) {
                        inStock.add(v);
                        variantProductMap.put(v, shopifyApi.getProduct(v));
                    } else {
                        numOutOfStock += variantNotificationMap.get(variantId).size();
                    }
			    }
			    
			    /* 5. Enqueue email notifications for all back in stock variants */
			    if(!inStock.isEmpty()) {
			        for(Variant v : inStock) {
            			    Product p = variantProductMap.get(v);
            			    // Get all unsent notifications for this variant
            			    List<Notification> variantNotifications = variantNotificationMap.remove(v.getId());
            			    for (Notification n: variantNotifications) {
            			        emailQueue.put(new EmailNotification(p,v,n));
            			        totalQueued++;
            			    }
			        }
            			synchronized(emailQueue) {
            			    emailQueue.notify(); // Notify once more in case of race condition
            			}
        			}
	            
        			/* 6. Standard Log Output: Summary for this iteration */ 
        			logger.message(String.format("Status: %s New Notification(s), %s Total, %s Sent, %s Unsent (%s Queued/%s Out of Stock)",
        			        numNew,
        			        allNotifications.size(),
        			        totalQueued-emailQueue.size(),
        			        emailQueue.size()+numOutOfStock,
        			        emailQueue.size(),
        			        numOutOfStock));
        			
			    /* 7. Sleep */
			    Thread.sleep(sleepMs);
			    
        			/* 8. Fetch new notifications */
			    notificationResponse = notificationsApi.getNewNotificationsSince(lastUpdate);
                newNotifications = notificationResponse.getNotifications();
                Iterator<Notification> newNotificationsIterator = newNotifications.iterator();
                while(newNotificationsIterator.hasNext()) {
                    Notification n = newNotificationsIterator.next();
                    // Handle duplicates
                    if(allNotifications.contains(n.toString()))
                        newNotificationsIterator.remove();
                    else 
                        allNotifications.add(n.toString());
                }
                lastUpdate = notificationResponse.getCurrentDate();
			}			
		};
	}
}