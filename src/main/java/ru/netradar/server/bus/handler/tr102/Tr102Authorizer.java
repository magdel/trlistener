package ru.netradar.server.bus.handler.tr102;

import ru.netradar.server.bus.domain.DeviceIden;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Created by rfk on 16.11.2017.
 */
public interface Tr102Authorizer {
    @Nonnull
    Optional<DeviceIden> identify(@Nonnull Tr102Iden tr102Iden);
}
