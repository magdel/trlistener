package ru.netradar.server.port.tr102;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.TCPServer;
import ru.netradar.utils.Utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by rfk on 15.11.2016.
 */

@Component
public class Tr102StringHandler extends ChannelHandlerAdapter implements StringEmitter {
    private static final Logger log = LoggerFactory.getLogger(TCPServer.class);

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
        if (trackerMessage.length() < 5) {
            log.info("Short msg:{}", trackerMessage);
            return;
        }
        if (stringFluxSink != null) {
            long count = readCounter.incrementAndGet();
            log.info("{} Read {}: count={}, msg={}", devImei, channelId, count, trackerMessage);
            stringFluxSink.next(trackerMessage);
        } else {
            log.warn("No emitter");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channelId = ctx.channel().remoteAddress().toString();
        log.info("Active: {}", channelId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("{} Inactive: {}", devImei, channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("{} On handle : {}", devImei, channelId, cause);
        ctx.close();
    }

    @Override
    public void accept(FluxSink<String> stringFluxSink) {
        log.info("Sink accepted");
        if (this.stringFluxSink != null) {
            throw new IllegalArgumentException("Already set");
        }
        this.stringFluxSink = stringFluxSink;
    }

    public long getReadCount() {
        return readCounter.get();
    }
}
