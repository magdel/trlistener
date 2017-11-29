package ru.netradar.server.port;

import ru.netradar.server.bus.domain.DeviceIden;

/**
 * Created by rfk on 29.11.2017.
 */
public interface ConnectionRegistry {
    void addConnection(NrConnection nrConnection);

    void removeConnection(NrConnection nrConnection);

    void addDeviceConnection(DeviceIden deviceIden, ConnectionId id);

    void removeDeviceConnection(DeviceIden deviceIden, ConnectionId id);
}
