package ru.netradar.server.notify.position;

import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.PositionQueueProperties;
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
public class PositionQueueDaemon extends QueueDaemon<PositionTask> {
    public PositionQueueDaemon(final TransactionTemplate txTemplate,
                               final TaskDAO<PositionTask> positionTaskTaskDAO,
                               final TaskExecutor<PositionTask> positionTaskTaskExecutor,
                               PositionQueueProperties positionQueueSettings) {
        super(txTemplate, positionTaskTaskDAO, positionTaskTaskExecutor, 5000, positionQueueSettings.getCount(), "Pos");
    }
}
