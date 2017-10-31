package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("monitor.recipients")
public class RecipientMonitorProperties {
    @NotNull
    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}

