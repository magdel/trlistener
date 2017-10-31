package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.*;
import ru.netradar.server.http.SiteClient;
import ru.netradar.server.notify.info.InfoQueueDaemon;
import ru.netradar.server.notify.info.InfoTaskDAO;
import ru.netradar.server.notify.info.InfoTaskExecutor;
import ru.netradar.server.notify.notification.NotificationQueueDaemon;
import ru.netradar.server.notify.notification.NotificationTaskDAO;
import ru.netradar.server.notify.notification.NotificationTaskExecutor;
import ru.netradar.server.notify.position.PositionQueueDaemon;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.notify.position.PositionTaskExecutor;
import ru.netradar.server.notify.sms.SMSQueueDaemon;
import ru.netradar.server.notify.sms.SMSTaskDAO;
import ru.netradar.server.notify.sms.SMSTaskExecutor;
import ru.netradar.utils.IdGenerator;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class QueueConfiguration {

    @Bean
    public PositionTaskExecutor positionTaskExecutor(PositionQueueProperties positionQueueProperties,
                                                     DeleteMonitorProperties dbDeleteMonitorSettings,
                                                     TransactionTemplate txTemplate,
                                                     PositionTaskDAO positionTaskTaskDAO,
                                                     SiteClient siteClient,
                                                     IdGenerator idGenerator) {
        return new PositionTaskExecutor(positionQueueProperties,
                dbDeleteMonitorSettings,
                txTemplate,
                positionTaskTaskDAO,
                siteClient,
                idGenerator);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public PositionQueueDaemon positionQueueDaemon(TransactionTemplate txTemplate,
                                                   PositionTaskDAO positionTaskTaskDAO,
                                                   PositionTaskExecutor positionTaskExecutor,
                                                   PositionQueueProperties positionQueueProperties) {
        return new PositionQueueDaemon(txTemplate, positionTaskTaskDAO, positionTaskExecutor,
                positionQueueProperties
        );
    }

    @Bean
    public InfoTaskExecutor infoTaskExecutor(InfoQueueProperties infoQueueProperties,
                                             DeleteMonitorProperties dbDeleteMonitorSettings,
                                             TransactionTemplate txTemplate,
                                             InfoTaskDAO infoTaskTaskDAO,
                                             SiteClient siteClient,
                                             IdGenerator idGenerator) {
        return new InfoTaskExecutor(infoQueueProperties,
                dbDeleteMonitorSettings,
                txTemplate,
                infoTaskTaskDAO,
                siteClient,
                idGenerator);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public InfoQueueDaemon infoQueueDaemon(TransactionTemplate txTemplate,
                                           InfoTaskDAO infoTaskTaskDAO,
                                           InfoTaskExecutor infoTaskExecutor,
                                           InfoQueueProperties infoQueueProperties) {
        return new InfoQueueDaemon(txTemplate, infoTaskTaskDAO, infoTaskExecutor,
                infoQueueProperties
        );
    }

    @Bean
    public NotificationTaskExecutor notificationTaskExecutor(NotificationQueueProperties notificationQueueProperties,
                                                             DeleteMonitorProperties dbDeleteMonitorSettings,
                                                             TransactionTemplate txTemplate,
                                                             NotificationTaskDAO notificationTaskTaskDAO,
                                                             SMSTaskExecutor smsTaskExecutor,
                                                             SiteClient siteClient,
                                                             IdGenerator idGenerator) {
        return new NotificationTaskExecutor(notificationQueueProperties,
                dbDeleteMonitorSettings,
                txTemplate,
                notificationTaskTaskDAO,
                smsTaskExecutor,
                siteClient,
                idGenerator);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public NotificationQueueDaemon notificationQueueDaemon(TransactionTemplate txTemplate,
                                                           NotificationTaskDAO notificationTaskTaskDAO,
                                                           NotificationTaskExecutor notificationTaskExecutor,
                                                           NotificationQueueProperties notificationQueueProperties) {
        return new NotificationQueueDaemon(txTemplate, notificationTaskTaskDAO,
                notificationTaskExecutor,
                notificationQueueProperties
        );
    }

    @Bean
    public SMSTaskExecutor smsTaskExecutor(TransactionTemplate txTemplate,
                                           SMSTaskDAO smsTaskDAO,
                                           SMSQueueProperties smsQueueProperties,
                                           IdGenerator idGenerator) {
        return new SMSTaskExecutor(
                txTemplate,
                smsTaskDAO,
                smsQueueProperties,
                idGenerator);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public SMSQueueDaemon smsQueueDaemon(TransactionTemplate txTemplate,
                                         SMSTaskDAO smsTaskDAO,
                                         SMSTaskExecutor smsTaskExecutor) {
        return new SMSQueueDaemon(txTemplate, smsTaskDAO, smsTaskExecutor);
    }

}
