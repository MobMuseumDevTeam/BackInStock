package com.chrisleung.notifications.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "my.notifications.restapi")
public class NotificationsApiConfig {
    public static class Param {
        private String sent;
        private String createdDate;
        public String getSent() {
            return sent;
        }
        public void setSent(String sent) {
            this.sent = sent;
        }
        public String getCreatedDate() {
            return createdDate;
        }
        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
        @Override
        public String toString() {
            return "Param [sent=" + sent + ", createdDate=" + createdDate + "]";
        }
    }
    private Param param;
    private String username;
    private String password;
    private String url;
    private Integer refresh;
    public Param getParam() {
        return param;
    }
    public void setParam(Param param) {
        this.param = param;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public Integer getRefresh() {
        return refresh;
    }
    public void setRefresh(Integer refresh) {
        this.refresh = refresh;
    }
    @Override
    public String toString() {
        return "RestApi [param=" + param + ", username=" + username + ", password=" + password + ", url=" + url
                + ", refresh=" + refresh + "]";
    }
}
