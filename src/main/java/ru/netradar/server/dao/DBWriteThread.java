/*
 * DBWriteThread.java
 *
 * Created on 18 ., 17:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.dao;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceDao;

import java.util.ArrayList;

/**
 * @author Raev
 */
public class DBWriteThread implements DiagInformation {

    private final static Logger LOG = Logger.getLogger(DBWriteThread.class);
    //public static DBWriteThread writeDBT;
    private ArrayList<String> commands = new ArrayList<String>();
    final Object queueSync = new Object();
    private final Runnable daemon;
    private Thread thrd;
    private final DiagThreadRegistry diagThreadRegistry;
 
    public DBWriteThread(final DeviceDao deviceDao, final TransactionTemplate txTemplate,
                         DiagThreadRegistry diagThreadRegistry) {
        this.diagThreadRegistry = diagThreadRegistry;
        //this.deviceDao=deviceDao;
        //this.txTemplate=txTemplate;

        this.daemon = new Runnable() {

            @Override
            public void run() {
                try {
                    while (!stopped) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            stopped = true;
                        }
                        if (commands.size() > 0) {
                            ArrayList<String> q;
                            synchronized (queueSync) {
                                q = commands;
                                commands = new ArrayList<String>();
                            }
                            final ArrayList<String> sqls = q;
                            final int[] rowsUpdated = new int[1];
                            long tm = System.currentTimeMillis();


                            txTemplate.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    for (String sql : sqls) {
                                        try {
                                            rowsUpdated[0] += deviceDao.executeSql(sql);
                                        } catch (Throwable yy) {
                                            LOG.error("Error sql: " + sql, yy);
                                        }
                                    }
                                }
                            });

                            tm = System.currentTimeMillis() - tm;
                            // System.out.println(String.valueOf(MNServerThread.conCount)+" users - "+ (new Time(System.currentTimeMillis())));
                            if (rowsUpdated[0] > 0) {
                                String rs = "Changed " + rowsUpdated[0] + " rows of " + q.size() + " for " + tm + " ms.";
                                LOG.info(rs);
                                diagStatus.updateStatusOK();
                            }
                        }
                    }
                    diagStatus.updateStatusBAD("Finished");

                } catch (Throwable ttt) {
                    diagStatus.updateStatusBAD("Error: " + ttt.toString());
                    LOG.error("DB write:" + ttt.getMessage(), ttt);

                }
            }
        };
    }

    public void init() {
        thrd = new Thread(daemon);
        thrd.start();
        diagThreadRegistry.registerThread(this);
    }

    public void shutdown() {
        if (thrd != null) {
            thrd.interrupt();
            LOG.info("Shutdowned");
        }
        diagThreadRegistry.unregisterThread(this);
    }

    public void put(String query) {
        synchronized (queueSync) {
            commands.add(query);
        }
    }

    private boolean stopped;

  /*  // Display an SQLException which has occured in this application.
    private static void showSQLException(java.sql.SQLException e) {
        // Notice that a SQLException is actually a chain of SQLExceptions,
        // let's not forget to print all of them...
        java.sql.SQLException next = e;
        while (next != null) {
            System.out.println(next.getMessage());
            System.out.println("Error Code: " + next.getErrorCode());
            System.out.println("SQL State: " + next.getSQLState());
            next = next.getNextException();
        }
    }*/

    @Override
    public String getDiagName() {
        return "Запись в DB";
    }

    @Override
    public String getDiagStatus() {
        return diagStatus.getStatusName();
    }

    @Override
    public String getDiagDescription() {
        return diagStatus.getStatusDesc();
    }

    private DiagStatus diagStatus = new DiagStatus();
}
