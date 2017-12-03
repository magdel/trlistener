package ru.netradar.server.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.netradar.server.bus.domain.DeviceIden;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rfk on 21.11.2017.
 */
@Component
public class ConnectionRegistryImpl implements ConnectionRegistry {
    private static final Logger log = LoggerFactory.getLogger(ConnectionRegistryImpl.class);
    private final ConcurrentHashMap<ConnectionId, NrConnection> registry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<DeviceIden, ConnectionId> deviceConnection = new ConcurrentHashMap<>();

    @Override
    public void addConnection(NrConnection nrConnection) {
        NrConnection replaced = registry.put(nrConnection.getId(), nrConnection);
        log.info("Put connection: {} {}", nrConnection, replaced);
    }

    @Override
    public void removeConnection(NrConnection nrConnection) {
        NrConnection removed = registry.remove(nrConnection.getId());
        log.info("Remove connection: {}", removed);
    }


    @Override
    public void addDeviceConnection(DeviceIden deviceIden, ConnectionId id) {
        ConnectionId replaced = deviceConnection.put(deviceIden, id);
        log.info("Put device: {} {}", deviceIden, replaced);

    }

    @Override
    public void removeDeviceConnection(DeviceIden deviceIden, ConnectionId id) {
        boolean removed = deviceConnection.remove(deviceIden, id);
        log.info("Removed device: {} {}", removed, deviceIden);
    }


    @Nullable
    public NrConnection getNrConnection(ConnectionId id) {
        return registry.get(id);
    }


}
