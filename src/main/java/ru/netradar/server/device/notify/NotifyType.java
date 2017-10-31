package ru.netradar.server.device.notify;

import ru.netradar.util.Enumeration;

public class NotifyType extends Enumeration {

    public static final NotifyType NOTIFY_BY_SMS = new NotifyType(1, "By SMS");

    public static final NotifyType NOTIFY_BY_CALL = new NotifyType(2, "By Call");

    public NotifyType(int ordinal, String name) {
        super(ordinal, name);
    }
}
