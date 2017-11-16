package ru.netradar.server.bus.handler.db.position;

import ru.netradar.server.bus.domain.IdentifiedLocationable;
import ru.netradar.server.port.tr102.Tr102Message;

import java.util.function.Consumer;

/**
 * Created by rfk on 16.11.2017.
 */
@FunctionalInterface
public interface IdentifiableLocationConsumer extends Consumer<IdentifiedLocationable> {
}
