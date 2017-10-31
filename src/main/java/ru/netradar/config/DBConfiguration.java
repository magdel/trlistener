package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.server.notify.info.InfoTaskDAO;
import ru.netradar.server.notify.notification.NotificationTaskDAO;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.notify.sms.SMSTaskDAO;
import ru.netradar.server.storage.DeviceDao;

import javax.sql.DataSource;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class DBConfiguration {

    @Bean
    public DeviceDao deviceDao(DataSource dataSource){
        return new DeviceDao(dataSource);
    }

    @Bean
    public PositionTaskDAO positionTaskDAO(DataSource dataSource){
        return new PositionTaskDAO(dataSource);
    }

    @Bean
    public InfoTaskDAO infoTaskDAO(DataSource dataSource){
        return new InfoTaskDAO(dataSource);
    }

    @Bean
    public NotificationTaskDAO notificationTaskDAO(DataSource dataSource){
        return new NotificationTaskDAO(dataSource);
    }

    @Bean
    public SMSTaskDAO smsTaskDAO(DataSource dataSource){
        return new SMSTaskDAO(dataSource);
    }


}
