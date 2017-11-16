package ru.netradar.server.bus.handler.db.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;
import ru.netradar.server.bus.domain.IdentifiedLocationable;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
public class IdentifiableLocationEmitterConsumer implements Consumer<FluxSink<IdentifiedLocationable>> {
    private static final Logger log = LoggerFactory.getLogger(IdentifiableLocationEmitterConsumer.class);
    private FluxSink<IdentifiedLocationable> tr102MessageFluxSink;

    @Override
    public void accept(FluxSink<IdentifiedLocationable> tr102MessageFluxSink) {
        log.info("Sink accepted");
        this.tr102MessageFluxSink = tr102MessageFluxSink;
    }

    public FluxSink<IdentifiedLocationable> getFluxSink() {
        return tr102MessageFluxSink;
    }
}
