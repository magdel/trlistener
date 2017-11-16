package ru.netradar.server.bus.handler.db.position;

import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.IdentifiedLocationable;

import java.util.function.Function;

/**
 * Created by rfk on 17.11.2017.
 */
@Component
public class PositionMapper implements Function<IdentifiedLocationable, String> {


    @Override
    public String apply(IdentifiedLocationable locationable) {
        return "pretend we stored in db";
        //Optional<DeviceIden> deviceIden = tr102Authorizer.identify(tr102Message.getTr102Iden());
        //return deviceIden.map(di -> new IdentifiedLocation(tr102Message.getNrLocation(), di));
    }
}
