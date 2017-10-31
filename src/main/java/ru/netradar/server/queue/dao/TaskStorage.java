package ru.netradar.server.queue.dao;

/**
 * Интерфейс хранилища заданий
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 20:27:09
 * To change this template use File | Settings | File Templates.
 */
public interface TaskStorage<T extends Task> {
    T fetchNextTask();

    void storeTask(T task);

    void deleteTask(T task);

    public int countTasks();
}
