package ru.netradar.server.port.tr102;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.netradar.server.port.TCPServer;
import ru.netradar.utils.IdGenerator;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 20.11.2017.
 */
public abstract class BaseAdapter extends ChannelHandlerAdapter {
    protected static final Logger log = LoggerFactory.getLogger(TCPServer.class);
    private final String channelId;

    public BaseAdapter(IdGenerator idGenerator) {
        this.channelId = "" + checkNotNull(idGenerator).generate();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String addr = "" + ctx.channel().remoteAddress().toString();
        log.info("Active: channelId={}, addr={}", channelId, addr);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("Inactive: id={}, channelId={}", getIdentificationString(), channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("On handle: channelId={}, id={}", channelId, getIdentificationString(), cause);
        ctx.close();
    }

    @Nullable
    protected abstract String getIdentificationString();
}
