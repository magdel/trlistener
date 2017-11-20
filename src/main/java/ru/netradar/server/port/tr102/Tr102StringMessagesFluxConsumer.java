package ru.netradar.server.port.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import ru.netradar.server.bus.handler.tr102.Tr102MessageConsumer;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by rfk on 15.11.2016.
 */
public class Tr102StringMessagesFluxConsumer {
    private static final Logger logger = LoggerFactory.getLogger(Tr102StringMessagesFluxConsumer.class);
    private final Consumer<FluxSink<String>> stringEmitterConsumer;
    private final Tr102MessageConsumer tr102MessageConsumer;
    private final StringToNrLocationRecordMapper mapper;
    private Cancellation cancellation;

    public Tr102StringMessagesFluxConsumer(StringEmitterConsumer stringEmitterConsumer,
                                           Tr102MessageConsumer tr102MessageConsumer,
                                           StringToNrLocationRecordMapper mapper) {
        this.stringEmitterConsumer = stringEmitterConsumer;
        this.tr102MessageConsumer = tr102MessageConsumer;
        this.mapper = mapper;
    }

    public void init() {
        cancellation = Flux.create(stringEmitterConsumer, FluxSink.OverflowStrategy.LATEST)
                //.log()
                //.onBackpressureBuffer(32)
                .publishOn(Schedulers.newSingle("mapper"))
                //.flatMap(str -> Mono.just(mapper.apply(str)))
                .map(mapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                //.log()
                .doOnComplete(() -> logger.warn("TR102 Stream completed"))
                .doOnError(throwable -> logger.error("TR102 Stream convert error", throwable))
                .subscribe(tr102MessageConsumer);
    }

    public void shutdown() {
        cancellation.dispose();
    }

}
