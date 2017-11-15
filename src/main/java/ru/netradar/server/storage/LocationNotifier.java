package ru.netradar.server.storage;

import ru.netradar.server.device.NRDevice;
import ru.netradar.server.bus.domain.NRLocation;

/**
 * Уведомитель координат
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 21.03.2010
 * Time: 18:42:32
 * To change this template use File | Settings | File Templates.
 */
public interface LocationNotifier {
    void notifyLocation(NRDevice device, NRLocation location);
}
