package ru.netradar.server.queue.dao;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Базовая даошка очередей
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 9:44:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class TaskDAO<T extends Task> extends JdbcDaoSupport implements TaskStorage<T> {
    private static final Logger LOG = Logger.getLogger(TaskDAO.class);

    protected static final String FIELDS_TASK = " id, next_send_dt, try_n, expiry_dt";

    protected final String FETCH_TASK;

    private final String DELETE_TASK;

    private final String COUNT_TASK;
    private final String tableName;

    @Override
    public void deleteTask(final T task) {
        getJdbcTemplate().update(DELETE_TASK, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;
                ps.setLong(++i, task.getId());
            }
        });
        LOG.debug("Deleted " + task);
    }

    @Override
    public int countTasks() {
        int count = getJdbcTemplate().queryForObject(COUNT_TASK, Integer.class);
        LOG.info("Count " + count + " for " + tableName);
        return count;
    }

    public TaskDAO(DataSource ds, String tableName, String fields) {
        this.tableName = tableName;
        setDataSource(ds);

        DELETE_TASK = "delete from " + tableName + " " +
                " where id = ? ";

        /*FETCH_TASK = "select next_send_dt, try_n, expiry_dt, t_id, " +
                fields +
                " from " + tableName + " " +
                "where next_send_dt < ? for update ";*/

        FETCH_TASK = "WITH cte AS (" +
                "SELECT id as id_cte " +
                "FROM " + tableName + " " +
                "WHERE next_send_dt <= now() " +
                "LIMIT 1 " +
                "FOR UPDATE SKIP LOCKED) " +
                "UPDATE " + tableName + " q " +
                "SET " +
                "  next_send_dt = next_send_dt + (try_n+1) * interval '2 minute', " +
                "  try_n = try_n + 1 " +
                "FROM cte " +
                "WHERE q.id = cte.id_cte " +
                "RETURNING  " + FIELDS_TASK + ", " + fields;


        COUNT_TASK = "select count(*) from  " + tableName;

        LOG.info("SQLS: \r\n" + FETCH_TASK + "\r\n" + DELETE_TASK + "\r\n" + COUNT_TASK);
    }
}
