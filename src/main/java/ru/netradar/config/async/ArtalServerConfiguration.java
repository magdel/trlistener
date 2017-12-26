package ru.netradar.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.server.port.ByteToStringDecoder;
import ru.netradar.server.port.ConnectionRegistry;
import ru.netradar.server.port.ServerLoopGroup;
import ru.netradar.server.port.TCPServer;
import ru.netradar.server.port.artal.ByteEmitterConsumer;
import ru.netradar.utils.IdGenerator;

/**
 * Created by rfk on 17.11.2017.
 */
@Configuration
public class ArtalServerConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public TCPServer artaltcpServer(ServerLoopGroup serverLoopGroup,
                                    AcceptorProperties acceptorProperties,
                                    ByteEmitterConsumer byteEmitterConsumer,
                                    IdGenerator idGenerator,
                                    ConnectionRegistry connectionRegistry) {
        return new TCPServer(acceptorProperties.getPortAsyncArtal(),
                serverLoopGroup,
                false,
                new ByteToStringDecoder());
    }

}
