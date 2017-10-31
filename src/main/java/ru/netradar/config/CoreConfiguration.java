package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.http.SiteClient;
import ru.netradar.server.storage.DeviceDao;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.server.storage.InfoNotifier;
import ru.netradar.server.storage.LocationNotifier;
import ru.netradar.utils.IdGenerator;
import ru.netradar.utils.SimpleFlakeIdGenerator;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class CoreConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return new SimpleFlakeIdGenerator();
    }


    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public DeviceStorage deviceStorage(DeviceDao deviceDao,
                                       LocationNotifier locationNotifier,
                                       InfoNotifier infoNotifier,
                                       TransactionTemplate txTemplate,
                                       WebMonitorProperties webMonitorProperties,
                                       SiteClient siteClient) {
        DeviceStorage deviceStorage = new DeviceStorage(deviceDao, locationNotifier, infoNotifier,
                txTemplate,
                webMonitorProperties);
        deviceStorage.setSiteClient(siteClient);
        return deviceStorage;
    }

    @Bean
    public SiteClient siteClient(WebMonitorProperties siteSettings) {
        return new SiteClient(siteSettings);
    }

    @Bean
    public DiagThreadRegistry diagThreadRegistry() {
        return new DiagThreadRegistry();
    }
}
