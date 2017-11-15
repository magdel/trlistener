package ru.netradar.server.port.tr102;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import ru.netradar.server.bus.domain.NRLocation;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by rfk on 15.11.2016.
 */
public class Tr102MessagesConsumer {
    private static final Logger logger = LoggerFactory.getLogger(Tr102MessagesConsumer.class);
    private final Consumer<FluxSink<String>> trackerStringHandler;
    private final Consumer<NRLocation> nrLocationConsumer;
    private final StringToNrLocationRecordMapper mapper;
    private Cancellation cancellation;

    public Tr102MessagesConsumer(Consumer<FluxSink<String>> trackerStringHandler,
                                 Consumer<NRLocation> nrLocationConsumer,
                                 StringToNrLocationRecordMapper mapper) {
        this.trackerStringHandler = trackerStringHandler;
        this.nrLocationConsumer = nrLocationConsumer;
        this.mapper = mapper;
    }

    public void init() {
        Consumer<FluxSink<String>> trackerStringHandler = this.trackerStringHandler;
        cancellation = Flux.create(trackerStringHandler, FluxSink.OverflowStrategy.LATEST)
                //.log()
                //.onBackpressureBuffer(32)
                .publishOn(Schedulers.newSingle("mapper"))
                //.flatMap(str -> Mono.just(mapper.apply(str)))
                .map(mapper)
                .filter(Optional::isPresent)
                .map(Optional::get)
                //.log()
                .doOnComplete(() -> logger.warn("Stream completed"))
                .doOnError(throwable -> logger.error("Stream convert error", throwable))
                .subscribe(nrLocationConsumer);
    }

    public void shutdown() {
        cancellation.dispose();
    }

}
