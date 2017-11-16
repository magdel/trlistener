package ru.netradar.server.bus.handler.db.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import ru.netradar.server.bus.domain.IdentifiedLocationable;
import ru.netradar.server.bus.handler.tr102.AuthorizedTr102Mapper;
import ru.netradar.server.bus.handler.tr102.Tr102MessageEmitterConsumer;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 16.11.2017.
 */
public class IdentifiableLocationConsumerFlux implements IdentifiableLocationConsumer {
    private static final Logger log = LoggerFactory.getLogger(IdentifiableLocationConsumerFlux.class);

    private final IdentifiableLocationEmitterConsumer emitter;
    private final PositionMapper positionMapper;
    private Cancellation cancellation;

    public IdentifiableLocationConsumerFlux(PositionMapper positionMapper) {
        this.emitter = new IdentifiableLocationEmitterConsumer();
        this.positionMapper = checkNotNull(positionMapper, "positionMapper");
    }

    public void init() {
        cancellation = Flux.create(emitter, FluxSink.OverflowStrategy.LATEST)
                //.log()
                //.onBackpressureBuffer(256)
                .publishOn(Schedulers.newParallel("dbPosition", 2))
                .map(positionMapper)// call db storage
                //.filter(Optional::isPresent)
                //.map(Optional::get)
                //.log()
                .doOnComplete(() -> log.warn("DB Locationable Stream completed"))
                .doOnError(throwable -> log.error("DB Locationable Stream error", throwable))
                .subscribe(res -> log.info("Position store result: {}", res));
    }

    @Override
    public void accept(IdentifiedLocationable locationable) {
        if (emitter.getFluxSink() != null) {
            emitter.getFluxSink().next(locationable);
        } else {
            log.warn("Emitter not ready");
        }
    }

    public void shutdown() {
        cancellation.dispose();
    }

}
