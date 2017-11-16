package ru.netradar.server.bus.handler.tr102;

import ru.netradar.server.port.tr102.Tr102Message;

import java.util.function.Consumer;

/**
 * Created by rfk on 16.11.2017.
 */
@FunctionalInterface
public interface Tr102MessageConsumer extends Consumer<Tr102Message> {
}
