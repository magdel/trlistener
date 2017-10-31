package ru.netradar.server;

import ru.netradar.util.Enumeration;

public class PayService extends Enumeration {
    public static final PayService SMS_NOTIFICATION = new PayService(1, "SMS Alert");

    public PayService(int code, String name) {
        super(code, name);
    }
}
