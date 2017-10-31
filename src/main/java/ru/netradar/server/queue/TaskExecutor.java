package ru.netradar.server.queue;

import ru.netradar.server.queue.dao.Task;

/**
 * Исполнитель задания
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 10:53:27
 * To change this template use File | Settings | File Templates.
 */
public interface TaskExecutor<T extends Task> {
    /**
     *
     * @param task task to execute
     * @return true, if completed
     */
    boolean processTask(T task);
}
