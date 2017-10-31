package ru.netradar.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
@ConfigurationProperties("monitor.clear")
public class DeleteMonitorProperties {

    @NotNull
    private String datadeleteurl;

    @NotNull
    private String rspass;

    @NotNull
    private Integer storedays;

    public String getDatadeleteurl() {
        return datadeleteurl;
    }

    public void setDatadeleteurl(String datadeleteurl) {
        this.datadeleteurl = datadeleteurl;
    }

    public String getRspass() {
        return rspass;
    }

    public void setRspass(String rspass) {
        this.rspass = rspass;
    }

    public Integer getStoredays() {
        return storedays;
    }

    public void setStoredays(Integer storedays) {
        this.storedays = storedays;
    }
}
