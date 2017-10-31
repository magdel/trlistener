package ru.netradar.server.notify.notification;

import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.NotificationQueueProperties;
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
public class NotificationQueueDaemon extends QueueDaemon<NotificationTask> {
    public NotificationQueueDaemon(final TransactionTemplate txTemplate,
                                   final TaskDAO<NotificationTask> taskDAO,
                                   final TaskExecutor<NotificationTask> taskExecutor,
                                   final NotificationQueueProperties notificationQueueProperties) {
        super(txTemplate, taskDAO, taskExecutor, 5000, notificationQueueProperties.getCount(),
                "Notify");
    }
}