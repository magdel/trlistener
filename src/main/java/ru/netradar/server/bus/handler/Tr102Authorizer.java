package ru.netradar.server.bus.handler;

import ru.netradar.server.bus.domain.DeviceIden;

/**
 * Created by rfk on 16.11.2017.
 */
public interface Tr102Authorizer {
    DeviceIden identify(Tr102Iden tr102Iden);
}
