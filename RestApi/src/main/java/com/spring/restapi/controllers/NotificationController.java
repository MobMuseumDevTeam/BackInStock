package com.spring.restapi.controllers;

import com.spring.restapi.models.Notification;
import com.spring.restapi.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * RESTful controller for accessing Notification data
 * 
 * <table>
 * <tr>td>Method</td><td>Endpoint</td><td>Notes</td></tr>
 * <tr><td>GET</td><td>/notifications</td><td>Get all notifications data</td></tr>
 * <tr><td>GET</td><td>/notifications/59be3c34b1a24167ad2779b5</td><td>Get single notification</td></tr>
 * <tr><td>POST</td><td>/notifications</td><td>Post data</td></tr>
 * <tr><td>PUT</td><td>/notifications/59be3c34b1a24167ad2779b5</td><td>Update data</td></tr>
 * <tr><td>DELETE</td><td>/products/59be3c34b1a24167ad2779b5</td><td>Delete data/<td></tr>
 * </table>
 * 
 * @author Chris Leung
 *
 */
@RestController
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

    @RequestMapping(method=RequestMethod.GET, value="/notifications")
    public Iterable<Notification> notification() {
        return notificationRepository.findAll();
    }

    @RequestMapping(method=RequestMethod.POST, value="/notifications")
    public String save(@RequestBody Notification notification) {
    	notificationRepository.save(notification);

        return notification.getId();
    }

    @RequestMapping(method=RequestMethod.GET, value="/notifications/{id}")
    public Notification show(@PathVariable String id) {
        return notificationRepository.findOne(id);
    }

    @RequestMapping(method=RequestMethod.PUT, value="/notifications/{id}")
    public Notification update(@PathVariable String id, @RequestBody Notification notification) {
    	Notification n = notificationRepository.findOne(id);
        if(notification.getCreatedDate() != null)
            n.setCreatedDate(notification.getCreatedDate());
        if(notification.getEmail() != null)
            n.setEmail(notification.getEmail());
        if(notification.getIsSent() != null)
            n.setIsSent(notification.getIsSent());
        if(notification.getSentDate() != null) {
        	n.setSentDate(notification.getSentDate());
        }
        if(notification.getSku() != null)
            n.setSku(notification.getSku());
        notificationRepository.save(n);
        return n;
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/notifications/{id}")
    public String delete(@PathVariable String id) {
    	Notification notification = notificationRepository.findOne(id);
        notificationRepository.delete(notification);

        return "notification deleted";
    }
}