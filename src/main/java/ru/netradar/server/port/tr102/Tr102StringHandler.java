package ru.netradar.server.port.tr102;

import io.netty.channel.ChannelHandlerContext;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.ConnectionData;
import ru.netradar.server.port.ConnectionRegistry;
import ru.netradar.utils.IdGenerator;
import ru.netradar.utils.Utils;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * YOU NEED NEW INSTANCE FOR EACH CONNECTION
 * Created by rfk on 15.11.2016.
 */

public class Tr102StringHandler extends BaseAdapter {

    private final AtomicLong readCounter = new AtomicLong();
    private final FluxSink<ConnectionData<String>> stringFluxSink;
    private String devImei = "";

    public Tr102StringHandler(IdGenerator idGenerator,
                              FluxSink<ConnectionData<String>> stringFluxSink,
                              ConnectionRegistry connectionRegistry) {
        super(idGenerator, connectionRegistry);
        this.stringFluxSink = stringFluxSink;
    }

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
            log.info("Read: id={}, count={}, msg={}", getIdentificationString(), count, trackerMessage);
            stringFluxSink.next(new ConnectionData<>(
                    getId(),
                    getDeviceIden(),
                    trackerMessage
            ));
        } else {
            log.warn("No emitter");
        }
    }

    @Override
    @Nullable
    protected String getIdentificationString() {
        return devImei;
    }

    public long getReadCount() {
        return readCounter.get();
    }
}
