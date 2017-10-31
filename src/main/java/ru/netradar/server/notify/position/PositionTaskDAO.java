package ru.netradar.server.notify.position;

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
 * 
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:17:15
 * To change this template use File | Settings | File Templates.
 */
public class PositionTaskDAO extends TaskDAO<PositionTask> {
    private static final Logger LOG = Logger.getLogger(PositionTaskDAO.class);
    private static final String FIELDS = "DEV_ID,UT,LAT,LON,ALT,SPD,CRS,DTTM";
    private static final String TABLE = "SENDQUEUE";
    private final String STORE_SQL;

    public PositionTaskDAO(DataSource ds) {
        super(ds, TABLE, FIELDS);
        STORE_SQL = "insert into " + TABLE + " (" + FIELDS + "," + FIELDS_TASK + ") " +
                " values (?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public PositionTask fetchNextTask() {
        final PositionTask[] tasks = new PositionTask[1];
        getJdbcTemplate().query(FETCH_TASK, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                int i = 0;

                Long id = rs.getLong(++i);
                Timestamp nextSendDt = rs.getTimestamp(++i);
                int tryN = rs.getInt(++i);
                Timestamp expiryDt = rs.getTimestamp(++i);

                int devId = rs.getInt(++i);
                int ut = rs.getInt(++i);
                float lat = rs.getFloat(++i);
                float lon = rs.getFloat(++i);
                int alt = rs.getInt(++i);
                float spd = rs.getFloat(++i);
                int crs = rs.getInt(++i);
                Timestamp dttm = rs.getTimestamp(++i);

                tasks[0] = new PositionTask(id, nextSendDt, tryN, expiryDt, devId, ut, lat, lon, alt, spd, crs, dttm);
            }
        });
        if (tasks[0] != null)
            LOG.info("Fetched " + tasks[0]);
        return tasks[0];
    }

    @Override
    public void storeTask(final PositionTask task) {
        getJdbcTemplate().update(STORE_SQL, new PreparedStatementSetter() {
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;
                
                ps.setInt(++i, task.getDevId());
                ps.setInt(++i, task.getUt());
                ps.setFloat(++i, task.getLat());
                ps.setFloat(++i, task.getLon());
                ps.setInt(++i, task.getAlt());
                ps.setFloat(++i, task.getSpd());
                ps.setInt(++i, task.getCrs());
                ps.setTimestamp(++i, task.getDttm());

                ps.setLong(++i, task.getId());
                ps.setTimestamp(++i, task.getNextSendDt());
                ps.setInt(++i, task.getTryN());
                ps.setTimestamp(++i, task.getExpiryDt());
            }
        });
    }


}
