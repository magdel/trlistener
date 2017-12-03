package ru.netradar.server.port.artal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.ConnectionData;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
@Component
public class ByteEmitterConsumer implements Consumer<FluxSink<ConnectionData<String>>> {
    private static final Logger log = LoggerFactory.getLogger(ByteEmitterConsumer.class);
    private FluxSink<ConnectionData<String>> tr102MessageFluxSink;

    @Override
    public void accept(FluxSink<ConnectionData<String>> tr102MessageFluxSink) {
        log.info("Sink accepted");
        this.tr102MessageFluxSink = tr102MessageFluxSink;
    }

    public FluxSink<ConnectionData<String>> getFluxSink() {
        return tr102MessageFluxSink;
    }
}
