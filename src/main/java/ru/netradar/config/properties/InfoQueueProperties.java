package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("info.queue")
public class InfoQueueProperties {

    @NotNull
    private String uri;

    @NotNull
    private Integer count;

    public String getUri() {
        return uri;
    }

    public Integer getCount() {
        return count;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
