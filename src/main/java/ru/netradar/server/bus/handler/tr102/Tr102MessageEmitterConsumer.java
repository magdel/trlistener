package ru.netradar.server.bus.handler.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
public class Tr102MessageEmitterConsumer implements Consumer<FluxSink<Tr102Message>> {
    private static final Logger log = LoggerFactory.getLogger(Tr102MessageEmitterConsumer.class);
    private FluxSink<Tr102Message> tr102MessageFluxSink;

    @Override
    public void accept(FluxSink<Tr102Message> tr102MessageFluxSink) {
        log.info("Sink accepted");
        this.tr102MessageFluxSink = tr102MessageFluxSink;
    }

    public FluxSink<Tr102Message> getFluxSink() {
        return tr102MessageFluxSink;
    }
}
