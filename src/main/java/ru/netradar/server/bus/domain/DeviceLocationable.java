package ru.netradar.server.bus.domain;

import javax.annotation.Nonnull;

/**
 * Created by rfk on 16.11.2017.
 */
@FunctionalInterface
public interface DeviceLocationable {
    @Nonnull
    NRLocation getDeviceLocation();
}
