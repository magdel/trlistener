package ru.netradar.server.notify.notification;

import ru.netradar.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 01.04.2010
 * Time: 20:19:52
 * To change this template use File | Settings | File Templates.
 */
public class NotificationPaymentStatus extends Enumeration {

    public static final NotificationPaymentStatus PAYED = new NotificationPaymentStatus(2, "PAYED");
    public static final NotificationPaymentStatus FAILED = new NotificationPaymentStatus(3, "FAILED");

    protected NotificationPaymentStatus(int ordinal, String name) {
        super(ordinal, name);
    }
}
