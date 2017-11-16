package ru.netradar.server.bus.handler.tr102;

import reactor.core.publisher.FluxSink;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.function.Consumer;

/**
 * Created by rfk on 17.11.2017.
 */
@FunctionalInterface
public interface Tr102MessageEmitter extends Consumer<FluxSink<Tr102Message>> {
}
