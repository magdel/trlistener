package ru.netradar.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.server.bus.handler.db.position.IdentifiableLocationConsumer;
import ru.netradar.server.bus.handler.tr102.AuthorizedTr102Mapper;
import ru.netradar.server.bus.handler.tr102.Tr102MessageConsumer;
import ru.netradar.server.bus.handler.tr102.Tr102MessageConsumerFlux;
import ru.netradar.server.port.ByteToStringDecoder;
import ru.netradar.server.port.ServerLoopGroup;
import ru.netradar.server.port.TCPServer;
import ru.netradar.server.port.tr102.StringToNrLocationRecordMapper;
import ru.netradar.server.port.tr102.Tr102StringHandler;
import ru.netradar.server.port.tr102.Tr102StringMessagesFluxConsumer;
import ru.netradar.utils.IdGenerator;

/**
 * Created by rfk on 17.11.2017.
 */
@Configuration
public class Tr102ServerConfiguration {

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public Tr102MessageConsumerFlux tr102MessageConsumerFlux(
            AuthorizedTr102Mapper authorizedTr102Mapper,
            IdentifiableLocationConsumer dbIdentifiableLocationConsumer) {
        return new Tr102MessageConsumerFlux(authorizedTr102Mapper,
                dbIdentifiableLocationConsumer);
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public Tr102StringMessagesFluxConsumer tr102StringMessagesFluxConsumer(
            Tr102StringHandler tr102StringHandler,
            Tr102MessageConsumer tr102MessageConsumer,
            IdGenerator idGenerator
    ) {
        return new Tr102StringMessagesFluxConsumer(tr102StringHandler,
                tr102MessageConsumer,
                new StringToNrLocationRecordMapper(idGenerator));
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public ServerLoopGroup serverLoopGroup() {
        return new ServerLoopGroup();
    }

    @Bean(initMethod = "init", destroyMethod = "shutdown")
    public TCPServer tr102tcpServer(ServerLoopGroup serverLoopGroup,
                                    Tr102StringHandler tr102StringHandler,
                                    AcceptorProperties acceptorProperties) {
        return new TCPServer(acceptorProperties.getPortAsyncTr102(),
                serverLoopGroup,
                true,
                new ByteToStringDecoder(),
                tr102StringHandler);
    }

}
