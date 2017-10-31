package ru.netradar.server.notify.notification;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import ru.netradar.server.device.notify.NotifyType;
import ru.netradar.server.queue.dao.TaskDAO;
import ru.netradar.util.Enumeration;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * dao
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:17:15
 * To change this template use File | Settings | File Templates.
 */
public class NotificationTaskDAO extends TaskDAO<NotificationTask> {
    private static final Logger LOG = Logger.getLogger(NotificationTaskDAO.class);
    private static final String FIELDS = "DEV_ID,UT,INFO,DTTM,ADDRESS,NOTIFY_TYPE,USER_ID";
    private static final String TABLE = "NOTIFYQUEUE";
    private final String STORE_SQL;

    public NotificationTaskDAO(DataSource ds) {
        super(ds, TABLE, FIELDS);
        STORE_SQL = "insert into " + TABLE + " (" + FIELDS + "," + FIELDS_TASK + ") " +
                " values (?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public NotificationTask fetchNextTask() {
        final NotificationTask[] tasks = new NotificationTask[1];
        getJdbcTemplate().query(FETCH_TASK,  new RowCallbackHandler() {
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
                        String address = rs.getString(++i);
                        //todo fix
                        NotifyType nt = (NotifyType)Enumeration.findByOrdinal(NotifyType.class, rs.getInt(++i));// rs.getInt(++i);
                        int userId = rs.getInt(++i);
                        tasks[0] = new NotificationTask(id, nextSendDt, tryN, expiryDt,
                                devId, ut, info, dttm,
                                address, nt, userId);
                    }
                }
        );
        if (tasks[0] != null)
            LOG.info("Fetched " + tasks[0]);
        return tasks[0];
    }

    @Override
    public void storeTask(final NotificationTask task) {
        getJdbcTemplate().update(STORE_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;

                ps.setInt(++i, task.getDevId());
                ps.setInt(++i, task.getUt());
                ps.setString(++i, task.getInfo());
                ps.setTimestamp(++i, task.getDttm());

                ps.setString(++i, task.getRecipientAddress());
                //todo fix
                ps.setInt(++i, task.getNotifyType().hashCode());
                ps.setInt(++i, task.getUserId());


                ps.setLong(++i, task.getId());
                ps.setTimestamp(++i, task.getNextSendDt());
                ps.setInt(++i, task.getTryN());
                ps.setTimestamp(++i, task.getExpiryDt());

            }
        });
    }


}