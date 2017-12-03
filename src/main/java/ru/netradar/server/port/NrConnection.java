package ru.netradar.server.port;

import ru.netradar.server.bus.domain.DeviceIden;

import javax.annotation.Nullable;

/**
 * Created by rfk on 21.11.2017.
 */
public interface NrConnection {
    ConnectionId getId();

    @Nullable
    DeviceIden getDeviceIden();

    void authorize(DeviceIden deviceIden);
}
