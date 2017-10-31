package ru.netradar.server.notify.sms;

import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.server.queue.QueueDaemon;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;

/**
 * Очередь уведомления
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:43:31
 * To change this template use File | Settings | File Templates.
 */
public class SMSQueueDaemon extends QueueDaemon<SMSTask> {
    public SMSQueueDaemon(final TransactionTemplate txTemplate,
                                   final TaskDAO<SMSTask> taskDAO,
                                   final TaskExecutor<SMSTask> taskExecutor) {
        super(txTemplate, taskDAO, taskExecutor, 5000, 1, "SMS");
    }
}