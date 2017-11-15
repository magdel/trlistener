package ru.netradar.server.bus.domain;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Created by rfk on 16.11.2017.
 */
@FunctionalInterface
public interface DeviceIdentifiable {
    @Nonnull
    DeviceIden getDeviceIden();
}
