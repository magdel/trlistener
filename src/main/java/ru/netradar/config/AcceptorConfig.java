package ru.netradar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.config.properties.SMSQueueProperties;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.*;
import ru.netradar.server.acceptor.viewer.ViewerService;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.storage.DeviceStorage;

/**
 * Created by rfk on 30.10.2017.
 */
@Configuration
public class AcceptorConfig {


    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public MNAccepterThread mnAccepterThread(AcceptorProperties acceptorsSettings,
                                             WebMonitorProperties webMonitorSettings,
                                             DeviceStorage deviceStorage,
                                             DiagThreadRegistry diagThreadRegistry) {
        return new MNAccepterThread(acceptorsSettings, webMonitorSettings, deviceStorage,
                diagThreadRegistry);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public TRAccepterThread trAccepterThread(AcceptorProperties acceptorsSettings,
                                             WebMonitorProperties webMonitorSettings,
                                             DeviceStorage deviceStorage,
                                             DiagThreadRegistry diagThreadRegistry) {
        return new TRAccepterThread(acceptorsSettings, diagThreadRegistry, webMonitorSettings,
                deviceStorage
        );
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public ARAccepterThread arAccepterThread(AcceptorProperties acceptorsSettings,
                                             WebMonitorProperties webMonitorSettings,
                                             DeviceStorage deviceStorage,
                                             DiagThreadRegistry diagThreadRegistry) {
        return new ARAccepterThread(acceptorsSettings, diagThreadRegistry, webMonitorSettings,
                deviceStorage
        );
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public DGAccepterThread dgAccepterThread(AcceptorProperties acceptorsSettings,
                                             SMSQueueProperties smsQueueProperties,
                                             DeviceStorage deviceStorage,
                                             DiagThreadRegistry diagThreadRegistry,
                                             PositionTaskDAO positionTaskDAO) {
        return new DGAccepterThread(acceptorsSettings, deviceStorage, diagThreadRegistry,
                smsQueueProperties, positionTaskDAO
        );
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public MNUDPAccepterThread mnudpAccepterThread(AcceptorProperties acceptorsSettings,
                                                   DeviceStorage deviceStorage,
                                                   DiagThreadRegistry diagThreadRegistry) {
        return new MNUDPAccepterThread(acceptorsSettings, deviceStorage, diagThreadRegistry
        );
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public ViewerService viewerService(AcceptorProperties acceptorsSettings,
                                       DeviceStorage deviceStorage,
                                       DiagThreadRegistry diagThreadRegistry) {
        return new ViewerService(acceptorsSettings, diagThreadRegistry, deviceStorage
        );
    }

}
