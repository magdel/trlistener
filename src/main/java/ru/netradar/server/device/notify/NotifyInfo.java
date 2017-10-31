package ru.netradar.server.device.notify;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Собранная инфа по отправке уведомлений
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 24.03.2010
 * Time: 17:39:01
 * To change this template use File | Settings | File Templates.
 */
public class NotifyInfo {
    Map<Integer, Recipient> recipients = new HashMap<Integer, Recipient>();
    Map<Integer, Recipient> unmodRecipients = Collections.unmodifiableMap(recipients);

    public void addRecipient(Recipient recipient) {
        recipients.put(recipient.getUserId(), recipient);
    }

    public Map<Integer, Recipient> getRecipients() {
        return unmodRecipients;
    }

    @Override
    public String toString() {
        return "NotifyInfo{" +
                "recipients=" + recipients +
                '}';
    }
}
