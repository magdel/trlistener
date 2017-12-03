package ru.netradar.server.port;

import javax.annotation.Nonnull;

/**
 * Created by rfk on 29.11.2017.
 */
public interface ConnectionIdHolder {
    @Nonnull
    ConnectionId getConnectionId();
}
