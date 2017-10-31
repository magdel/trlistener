package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("sms.queue")
public class SMSQueueProperties {
    @NotNull
    private String sender;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

