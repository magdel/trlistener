package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("web")
public class WebMonitorProperties {

    @NotNull
    private String usercheckurl;

    @NotNull
    private String listuserurl;

    @NotNull
    private String host;

    @NotNull
    private Integer timeout;

    public String getUsercheckurl() {
        return usercheckurl;
    }

    public void setUsercheckurl(String usercheckurl) {
        this.usercheckurl = usercheckurl;
    }

    public String getListuserurl() {
        return listuserurl;
    }

    public void setListuserurl(String listuserurl) {
        this.listuserurl = listuserurl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
