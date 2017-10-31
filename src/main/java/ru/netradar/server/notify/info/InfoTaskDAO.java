package ru.netradar.server.notify.info;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import ru.netradar.server.queue.dao.TaskDAO;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:17:15
 * To change this template use File | Settings | File Templates.
 */
public class InfoTaskDAO extends TaskDAO<InfoTask> {
    private static final Logger LOG = Logger.getLogger(InfoTaskDAO.class);
    private static final String FIELDS = "DEV_ID,UT,INFO,DTTM";
    private static final String TABLE = "MSGQUEUE";
    private final String STORE_SQL;

    public InfoTaskDAO(DataSource ds) {
        super(ds, TABLE, FIELDS);
        STORE_SQL = "insert into " + TABLE + " (" + FIELDS + "," + FIELDS_TASK + ") " +
                " values (?,?,?,?,?,?,?,?)";
    }

    @Override
    public InfoTask fetchNextTask() {
        final InfoTask[] tasks = new InfoTask[1];
        getJdbcTemplate().query(FETCH_TASK, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                int i = 0;

                Long id = rs.getLong(++i);
                Timestamp nextSendDt = rs.getTimestamp(++i);
                int tryN = rs.getInt(++i);
                Timestamp expiryDt = rs.getTimestamp(++i);

                int devId = rs.getInt(++i);
                int ut = rs.getInt(++i);
                String info = rs.getString(++i);
                Timestamp dttm = rs.getTimestamp(++i);

                tasks[0] = new InfoTask(id, nextSendDt, tryN, expiryDt, devId, ut, info, dttm);
            }
        });
        if (tasks[0] != null) {
            LOG.info("Fetched " + tasks[0]);
        }
        return tasks[0];
    }

    @Override
    public void storeTask(final InfoTask task) {
        getJdbcTemplate().update(STORE_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;

                ps.setInt(++i, task.getDevId());
                ps.setInt(++i, task.getUt());
                ps.setString(++i, task.getInfo());
                ps.setTimestamp(++i, task.getDttm());

                ps.setLong(++i, task.getId());
                ps.setTimestamp(++i, task.getNextSendDt());
                ps.setInt(++i, task.getTryN());
                ps.setTimestamp(++i, task.getExpiryDt());
            }
        });
    }


}