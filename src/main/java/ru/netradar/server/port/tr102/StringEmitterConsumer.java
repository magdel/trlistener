package ru.netradar.server.port.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
@Component
public class StringEmitterConsumer implements Consumer<FluxSink<String>> {
    private static final Logger log = LoggerFactory.getLogger(StringEmitterConsumer.class);
    private FluxSink<String> tr102MessageFluxSink;

    @Override
    public void accept(FluxSink<String> tr102MessageFluxSink) {
        log.info("Sink accepted");
        this.tr102MessageFluxSink = tr102MessageFluxSink;
    }

    public FluxSink<String> getFluxSink() {
        return tr102MessageFluxSink;
    }
}
