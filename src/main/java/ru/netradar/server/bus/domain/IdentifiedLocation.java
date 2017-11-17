package ru.netradar.server.bus.domain;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Created by rfk on 16.11.2017.
 */
public class IdentifiedLocation implements IdentifiedLocationable {

    private final NRLocation location;
    private final DeviceIden deviceIden;

    public IdentifiedLocation(@Nonnull NRLocation location,
                              @Nonnull DeviceIden deviceIden) {
        this.location = Objects.requireNonNull(location);
        this.deviceIden = Objects.requireNonNull(deviceIden);
    }

    @Nonnull
    @Override
    public NRLocation getDeviceLocation() {
        return location;
    }

    @Nonnull
    @Override
    public DeviceIden getDeviceIden() {
        return deviceIden;
    }

    @Override
    public String toString() {
        return "IdentifiedLocation{" +
                "location=" + location +
                ", deviceIden=" + deviceIden +
                '}';
    }
}
