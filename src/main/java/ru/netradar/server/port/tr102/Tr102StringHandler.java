package ru.netradar.server.port.tr102;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.TCPServer;
import ru.netradar.utils.Utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Created by rfk on 15.11.2016.
 */

public class Tr102StringHandler extends ChannelHandlerAdapter implements Consumer<FluxSink<String>> {
    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private final AtomicLong readCounter = new AtomicLong();
    private FluxSink<String> stringFluxSink;
    private String channelId = "unknown";
    private String devImei = "";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String trackerMessage = (String) msg;
        int indexOf = trackerMessage.indexOf(',');
        if (indexOf > 0) {
            devImei = Utils.cutString(trackerMessage, indexOf);
        }
        if (stringFluxSink != null) {
            long count = readCounter.incrementAndGet();
            logger.info("{} Read {}: count={}, msg={}", devImei, channelId, count, trackerMessage);
            stringFluxSink.next(trackerMessage);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channelId = ctx.channel().remoteAddress().toString();
        logger.info("Active: {}", channelId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("{} Inactive: {}", devImei, channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("{} On handle : {}", devImei, channelId, cause);
        ctx.close();
    }

    @Override
    public void accept(FluxSink<String> stringFluxSink) {
        if (this.stringFluxSink != null) {
            throw new IllegalArgumentException("Already set");
        }
        this.stringFluxSink = stringFluxSink;
    }

    public long getReadCount() {
        return readCounter.get();
    }
}
