package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.config.properties.RecipientMonitorProperties;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.monitor.*;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.server.storage.NotificationNotifier;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class MonitorConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public DBDeleteMonitorThread dbDeleteMonitorThread(DeleteMonitorProperties dbDeleteMonitorSettings,
                                                       DeviceStorage deviceStorage,
                                                       DiagThreadRegistry diagThreadRegistry
    ) {
        return new DBDeleteMonitorThread(dbDeleteMonitorSettings, "CLEARMON", deviceStorage,
                diagThreadRegistry);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public SMSActivityMonitorThread smsActivityMonitorThread(NotificationNotifier notificationNotifier,
                                                             DeviceStorage deviceStorage,
                                                             DiagThreadRegistry diagThreadRegistry
    ) {
        return new SMSActivityMonitorThread(notificationNotifier, deviceStorage,
                diagThreadRegistry);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public RecipientsMonitorThread recipientsMonitorThread(DeviceStorage deviceStorage,
                                                           DiagThreadRegistry diagThreadRegistry,
                                                           RecipientMonitorProperties recipientMonitorProperties,
                                                           DeleteMonitorProperties dbDeleteMonitorSettings
    ) {
        return new RecipientsMonitorThread(deviceStorage,
                diagThreadRegistry, recipientMonitorProperties, dbDeleteMonitorSettings);
    }


}
