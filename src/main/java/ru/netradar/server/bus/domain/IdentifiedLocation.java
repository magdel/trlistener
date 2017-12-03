package ru.netradar.server.bus.domain;

import ru.netradar.server.port.ConnectionId;
import ru.netradar.server.port.ConnectionIdHolder;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 16.11.2017.
 */
public class IdentifiedLocation implements IdentifiedLocationable, ConnectionIdHolder {

    private final NRLocation location;
    private final DeviceIden deviceIden;
    private final ConnectionId connectionId;

    public IdentifiedLocation(@Nonnull NRLocation location,
                              @Nonnull DeviceIden deviceIden,
                              @Nonnull ConnectionId connectionId) {
        this.location = checkNotNull(location);
        this.deviceIden = checkNotNull(deviceIden);
        this.connectionId = checkNotNull(connectionId);
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

    @Nonnull
    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Override
    public String toString() {
        return "IdentifiedLocation{" +
                "location=" + location +
                ", deviceIden=" + deviceIden +
                ", connectionId=" + connectionId +
                '}';
    }
}
