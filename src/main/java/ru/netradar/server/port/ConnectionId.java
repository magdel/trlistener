package ru.netradar.server.port;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rfk on 29.11.2017.
 */
public class ConnectionId {
    private final Long value;

    public ConnectionId(@Nonnull Long value) {
        this.value = checkNotNull(value, "value");
    }

    @Nonnull
    public Long value() {
        return value;
    }

    @Override
    public String toString() {
        return "ConnectionId{" +
                "value=" + value +
                '}';
    }
}
