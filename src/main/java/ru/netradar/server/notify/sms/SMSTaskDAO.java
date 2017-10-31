package ru.netradar.server.notify.sms;

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
 * sms task
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:17:15
 * To change this template use File | Settings | File Templates.
 */
public class SMSTaskDAO extends TaskDAO<SMSTask> {
    private static final Logger LOG = Logger.getLogger(SMSTaskDAO.class);
    private static final String FIELDS = "INFO,ADDRESS,N_ID";
    private static final String TABLE = "SMSQUEUE";
    private final String STORE_SQL;

    public SMSTaskDAO(DataSource ds) {
        super(ds, TABLE, FIELDS);
        STORE_SQL = "insert into " + TABLE + " (" + FIELDS + "," + FIELDS_TASK + ") " +
                " values (?,?,?,?,?,?,?)";
    }

    @Override
    public SMSTask fetchNextTask() {
        final SMSTask[] tasks = new SMSTask[1];
        getJdbcTemplate().query(FETCH_TASK, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                int i = 0;

                Long id = rs.getLong(++i);
                Timestamp nextSendDt = rs.getTimestamp(++i);
                int tryN = rs.getInt(++i);
                Timestamp expiryDt = rs.getTimestamp(++i);

                String info = rs.getString(++i);
                String address = rs.getString(++i);
                long nId = rs.getLong(++i);
                tasks[0] = new SMSTask(id, nextSendDt, tryN, expiryDt,
                        info, address, nId);
            }
        });
        if (tasks[0] != null) {
            LOG.info("Fetched " + tasks[0]);
        }
        return tasks[0];
    }

    @Override
    public void storeTask(final SMSTask task) {
        getJdbcTemplate().update(STORE_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;

                ps.setString(++i, task.getInfo());
                ps.setString(++i, task.getRecipientAddress());
                ps.setLong(++i, task.getNId());

                ps.setLong(++i, task.getId());
                ps.setTimestamp(++i, task.getNextSendDt());
                ps.setInt(++i, task.getTryN());
                ps.setTimestamp(++i, task.getExpiryDt());

            }
        });
    }


}