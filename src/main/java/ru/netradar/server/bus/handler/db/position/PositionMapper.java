package ru.netradar.server.bus.handler.db.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.IdentifiedLocationable;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;

import java.util.function.Function;

/**
 * Created by rfk on 17.11.2017.
 */
@Component
public class PositionMapper implements Function<IdentifiedLocationable, String> {
private static final Logger log = LoggerFactory.getLogger(PositionMapper.class);
    private final DeviceStorage deviceStorage;

    public PositionMapper(@Autowired DeviceStorage deviceStorage) {
        this.deviceStorage = deviceStorage;
    }

    @Override
    public String apply(IdentifiedLocationable locationable) {
        //return "pretend we stored in db";
        log.info("Storing: {}", locationable);
        NRDevice nrDevice = deviceStorage.getNRLocation(
                new NRObject(locationable.getDeviceIden().userId,
                        locationable.getDeviceIden().getUserType().getAsByte())
        );
        deviceStorage.notifyPosition(nrDevice, locationable.getDeviceLocation());
        return "Stored OK";
    }
}
