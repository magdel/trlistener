package ru.netradar.server.port.tr102;

import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.bus.handler.tr102.Tr102Iden;
import ru.netradar.server.port.ConnectionId;
import ru.netradar.server.port.ConnectionIdHolder;
import ru.netradar.server.port.DeviceIdenHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 16.11.2017.
 */
public class Tr102Message implements ConnectionIdHolder, DeviceIdenHolder {
    private final Tr102Iden tr102Iden;
    private final NRLocation nrLocation;
    private final ConnectionId connectionId;
    @Nullable
    private final DeviceIden deviceIden;

    public Tr102Message(Tr102Iden tr102Iden,
                        NRLocation nrLocation,
                        ConnectionId connectionId,
                        @Nullable DeviceIden deviceIden) {
        this.tr102Iden = checkNotNull(tr102Iden);
        this.nrLocation = checkNotNull(nrLocation);
        this.connectionId = checkNotNull(connectionId);
        this.deviceIden = deviceIden;
    }

    public Tr102Iden getTr102Iden() {
        return tr102Iden;
    }

    public NRLocation getNrLocation() {
        return nrLocation;
    }

    @Nonnull
    @Override
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    @Nonnull
    @Override
    public Optional<DeviceIden> getDeviceIden() {
        return Optional.ofNullable(deviceIden);
    }

    @Override
    public String toString() {
        return "Tr102Message{" +
                "tr102Iden=" + tr102Iden +
                ", nrLocation=" + nrLocation +
                ", connectionId=" + connectionId +
                ", deviceIden=" + deviceIden +
                '}';
    }
}
