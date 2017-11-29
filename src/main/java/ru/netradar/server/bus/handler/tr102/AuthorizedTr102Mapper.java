package ru.netradar.server.bus.handler.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.bus.domain.IdentifiedLocation;
import ru.netradar.server.port.ConnectionRegistryImpl;
import ru.netradar.server.port.NrConnection;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.Optional;
import java.util.function.Function;

/**
 * Created by rfk on 17.11.2017.
 */
@Component
public class AuthorizedTr102Mapper implements Function<Tr102Message, Optional<IdentifiedLocation>> {
    private static final Logger log = LoggerFactory.getLogger(AuthorizedTr102Mapper.class);
    private final Tr102Authorizer tr102Authorizer;
    private final ConnectionRegistryImpl connectionRegistry;

    public AuthorizedTr102Mapper(@Autowired Tr102Authorizer tr102Authorizer,
                                 ConnectionRegistryImpl connectionRegistry) {
        this.tr102Authorizer = tr102Authorizer;
        this.connectionRegistry = connectionRegistry;
    }

    @Override
    public Optional<IdentifiedLocation> apply(Tr102Message tr102Message) {
        if (tr102Message.getDeviceIden().isPresent()) {
            return Optional.of(new IdentifiedLocation(
                    tr102Message.getNrLocation(),
                    tr102Message.getDeviceIden().get(),
                    tr102Message.getConnectionId()
            ));
        }
        log.info("Authorizing: {}", tr102Message);
        Optional<DeviceIden> deviceIden = tr102Authorizer.identify(tr102Message.getTr102Iden());
        if (deviceIden.isPresent()) {
            NrConnection nrConnection = connectionRegistry.getNrConnection(tr102Message.getConnectionId());
            if (nrConnection != null) {
                nrConnection.authorize(deviceIden.get());
            }
        }

        return deviceIden.map(di -> new IdentifiedLocation(
                tr102Message.getNrLocation(),
                di,
                tr102Message.getConnectionId()
        ));
    }
}
