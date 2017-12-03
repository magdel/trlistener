package ru.netradar.server.port;

import ru.netradar.server.bus.domain.DeviceIden;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 29.11.2017.
 */
public class ConnectionData<T> implements ConnectionIdHolder, DeviceIdenHolder {
    private final ConnectionId connectionId;
    @Nullable
    private final DeviceIden deviceIden;
    private final T data;

    public ConnectionData(@Nonnull ConnectionId connectionId,
                          @Nullable DeviceIden deviceIden,
                          @Nonnull T data) {
        this.connectionId = checkNotNull(connectionId, "connectionId");
        this.deviceIden = deviceIden;
        this.data = checkNotNull(data, "data");
    }

    @Override
    @Nonnull
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Nonnull
    public T getData() {
        return data;
    }

    @Nonnull
    @Override
    public Optional<DeviceIden> getDeviceIden() {
        return Optional.ofNullable(deviceIden);
    }

    @Override
    public String toString() {
        return "ConnectionData{" +
                "connectionId=" + connectionId +
                ", deviceIden=" + deviceIden +
                ", data=" + data +
                '}';
    }
}
