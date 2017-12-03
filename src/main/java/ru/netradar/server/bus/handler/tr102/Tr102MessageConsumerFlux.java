package ru.netradar.server.bus.handler.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import ru.netradar.server.bus.handler.db.position.IdentifiableLocationConsumer;
import ru.netradar.server.port.ConnectionRegistryImpl;
import ru.netradar.server.port.NrConnection;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 16.11.2017.
 */
public class Tr102MessageConsumerFlux implements Tr102MessageConsumer {
    private static final Logger log = LoggerFactory.getLogger(Tr102MessageConsumerFlux.class);

    private final Tr102MessageEmitterConsumer emitter;
    private final AuthorizedTr102Mapper authorizedTr102Mapper;
    private final IdentifiableLocationConsumer identifiableLocationConsumer;
    private final ConnectionRegistryImpl connectionRegistry;
    private Cancellation cancellation;

    public Tr102MessageConsumerFlux(AuthorizedTr102Mapper authorizedTr102Mapper,
                                    IdentifiableLocationConsumer identifiableLocationConsumer,
                                    ConnectionRegistryImpl connectionRegistry) {
        this.identifiableLocationConsumer = identifiableLocationConsumer;
        this.connectionRegistry = connectionRegistry;
        this.emitter = new Tr102MessageEmitterConsumer();
        this.authorizedTr102Mapper = checkNotNull(authorizedTr102Mapper, "authorizedTr102Mapper");
    }

    public void init() {
        cancellation = Flux.create(emitter, FluxSink.OverflowStrategy.LATEST)
                //.log()
                //.onBackpressureBuffer(32)
                .publishOn(Schedulers.newParallel("tr102messager", 4))
                //.flatMap(str -> Mono.just(mapper.apply(str)))
                .map(authorizedTr102Mapper)// authorize transformer
                .filter(Optional::isPresent)
                .map(Optional::get)
                //.log()
                .doOnComplete(() -> log.warn("TR102 Messager Stream completed"))
                .doOnError(throwable -> log.error("TR102 Messager Stream error", throwable))
                .subscribe(identifiableLocationConsumer);//consume in db
    }


    @Override
    public void accept(Tr102Message tr102Message) {
        if (emitter.getFluxSink() != null) {
            emitter.getFluxSink().next(tr102Message);
        } else {
            log.warn("Emitter not ready");
        }
    }

    public void shutdown() {
        cancellation.dispose();
    }

}
