package ru.netradar.server.notify.info;

import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.InfoQueueProperties;
import ru.netradar.server.queue.QueueDaemon;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;

/**
 * Очередь координат
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:43:31
 * To change this template use File | Settings | File Templates.
 */
public class InfoQueueDaemon extends QueueDaemon<InfoTask> {
    public InfoQueueDaemon(final TransactionTemplate txTemplate,
                           final TaskDAO<InfoTask> taskDAO,
                           final TaskExecutor<InfoTask> taskExecutor,
                           final InfoQueueProperties infoQueueProperties) {
        super(txTemplate, taskDAO, taskExecutor, 5000, infoQueueProperties.getCount(), "Info");
    }
}