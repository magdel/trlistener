package ru.netradar.server.port.tr102;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.port.ConnectionId;
import ru.netradar.server.port.ConnectionRegistry;
import ru.netradar.server.port.NrConnection;
import ru.netradar.server.port.TCPServer;
import ru.netradar.utils.IdGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 20.11.2017.
 */
public abstract class BaseAdapter extends ChannelHandlerAdapter implements NrConnection {
    protected static final Logger log = LoggerFactory.getLogger(TCPServer.class);
    @Nonnull
    private final ConnectionId channelId;
    @Nonnull
    private final ConnectionRegistry connectionRegistry;

    @Nullable
    private volatile DeviceIden deviceIden;

    public BaseAdapter(IdGenerator idGenerator, ConnectionRegistry connectionRegistry) {
        this.channelId = new ConnectionId(checkNotNull(idGenerator, "idGenerator").generate());
        this.connectionRegistry = checkNotNull(connectionRegistry, "connectionRegistry");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String addr = "" + ctx.channel().remoteAddress().toString();
        connectionRegistry.addConnection(this);
        log.info("Active: channelId={}, addr={}", channelId, addr);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (deviceIden != null) {
            connectionRegistry.removeDeviceConnection(deviceIden, channelId);
        }
        connectionRegistry.removeConnection(this);
        log.info("Inactive: id={}, channelId={}", getIdentificationString(), channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        connectionRegistry.removeConnection(this);
        log.error("On handle: channelId={}, id={}", channelId, getIdentificationString(), cause);
        ctx.close();
    }

    @Nullable
    protected abstract String getIdentificationString();


    @Override
    @Nonnull
    public ConnectionId getId() {
        return channelId;
    }

    @Nullable
    @Override
    public DeviceIden getDeviceIden() {
        return deviceIden;
    }

    @Override
    public void authorize(DeviceIden deviceIden) {
        if (this.deviceIden == null) {
            this.deviceIden = deviceIden;
            connectionRegistry.addDeviceConnection(deviceIden, channelId);
            log.info("Authorized: {}", getId());
        }
    }
}
