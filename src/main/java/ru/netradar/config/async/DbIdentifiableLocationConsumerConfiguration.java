package ru.netradar.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.server.bus.handler.db.position.IdentifiableLocationConsumer;
import ru.netradar.server.bus.handler.db.position.IdentifiableLocationConsumerFlux;
import ru.netradar.server.bus.handler.db.position.PositionMapper;

/**
 * Created by rfk on 17.11.2017.
 */
@Configuration
public class DbIdentifiableLocationConsumerConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public IdentifiableLocationConsumer dbIdentifiableLocationConsumer(PositionMapper positionMapper) {
        return new IdentifiableLocationConsumerFlux(positionMapper);
    }
}
