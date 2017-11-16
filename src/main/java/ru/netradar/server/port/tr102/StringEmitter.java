package ru.netradar.server.port.tr102;

import reactor.core.publisher.FluxSink;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
@FunctionalInterface
public interface StringEmitter extends Consumer<FluxSink<String>> {
}
