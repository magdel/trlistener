package ru.netradar.server.port;

import ru.netradar.server.bus.domain.DeviceIden;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Created by rfk on 29.11.2017.
 */
public interface DeviceIdenHolder {
    @Nonnull
    Optional<DeviceIden> getDeviceIden();
}
