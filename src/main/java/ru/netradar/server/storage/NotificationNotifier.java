package ru.netradar.server.storage;

import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.notify.Recipient;

/**
 * Уведомитель координат
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 21.03.2010
 * Time: 18:42:32
 * To change this template use File | Settings | File Templates.
 */
public interface NotificationNotifier {
    void createNotification(NRDevice device, Recipient recipient, String info);
}