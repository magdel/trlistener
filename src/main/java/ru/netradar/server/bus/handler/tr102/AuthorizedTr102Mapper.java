package ru.netradar.server.bus.handler.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.bus.domain.IdentifiedLocation;
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

    public AuthorizedTr102Mapper(@Autowired Tr102Authorizer tr102Authorizer) {
        this.tr102Authorizer = tr102Authorizer;
    }

    @Override
    public Optional<IdentifiedLocation> apply(Tr102Message tr102Message) {
        log.info("Authorizing: {}", tr102Message);
        Optional<DeviceIden> deviceIden = tr102Authorizer.identify(tr102Message.getTr102Iden());
        return deviceIden.map(di -> new IdentifiedLocation(tr102Message.getNrLocation(), di));
    }
}
