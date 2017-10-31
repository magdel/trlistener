package ru.netradar.server.notify.sms;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.SMSQueueProperties;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;
import ru.netradar.utils.IdGenerator;
import ru.smstraffic.smsclient.FatalSmsException;
import ru.smstraffic.smsclient.Sms;
import ru.smstraffic.smsclient.SmsException;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Доставка положения
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:05:07
 * To change this template use File | Settings | File Templates.
 */
public class SMSTaskExecutor implements TaskExecutor<SMSTask> {
    private static final Logger LOG = Logger.getLogger(SMSTaskExecutor.class);
    private static final long MINUTE = 60000;
    private static final long DAY = 24 * 60 * MINUTE;
    private static final long WEEK = 7 * DAY;
    private final TransactionTemplate txTemplate;
    private final TaskDAO<SMSTask> taskDAO;
    private final String smsSender;
    private final IdGenerator idGenerator;

    public SMSTaskExecutor(TransactionTemplate txTemplate, TaskDAO<SMSTask> positionTaskTaskDAO,
                           SMSQueueProperties smsQueueSettings,
                           IdGenerator idGenerator) {
        this.txTemplate = txTemplate;
        this.taskDAO = positionTaskTaskDAO;
        this.smsSender = smsQueueSettings.getSender();


        this.idGenerator = idGenerator;
    }

    protected void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (true) {//Main.noStartSms
                    LOG.info("No start SMS sent.");
                } else {

                    try {
                        final String s = Sms.send("79119159380,79626857771", "Сервер <ver>  запущен " + (new Date()), smsSender, 1);
                        //final String s2 = Sms.send("79626857771", "Сервер запущен " + (new Date()), smsSender, 1);
                        LOG.info("SMS Start id: " + s);
                    } catch (SmsException e) {
                        LOG.error("SMS Start: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    @Override
    public boolean processTask(SMSTask task) {
        if (task.getExpiryDt().getTime() < System.currentTimeMillis()) {
            LOG.warn("Task expired " + task);
            return true;
        }

        try {
            final String s = Sms.send(task.getRecipientAddress(), task.getInfo(), smsSender, 1);
            LOG.info("SMS id: " + s);
        } catch (SmsException e) {
            if (e instanceof FatalSmsException) {
                LOG.info("SMS send: " + e.getMessage());
                return true;
            }
            if (task.getTryN() < 10) {
                return false;
            }
            return true;
        }
        return true;
    }

    public void createNotification(String recipientAddress, String info, long nId) {
        final long time = System.currentTimeMillis();
        final SMSTask task = new SMSTask(idGenerator.generate(),
                new Timestamp(time), 0,
                new Timestamp(System.currentTimeMillis() + WEEK),
                info, recipientAddress, nId);

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                taskDAO.storeTask(task);
            }
        });
        LOG.info("Notification for " + recipientAddress + ": " + task.getInfo());
    }
}