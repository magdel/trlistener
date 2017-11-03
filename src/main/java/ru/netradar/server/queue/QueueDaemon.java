package ru.netradar.server.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.server.queue.dao.Task;
import ru.netradar.server.queue.dao.TaskStorage;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Выбиратель заданий
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 9:31:29
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueueDaemon<T extends Task> {
    private final static Logger LOG = LoggerFactory.getLogger(QueueDaemon.class);
    private final Runnable thrdRunnable;
    private final Thread[] thrds;
    private final Object syncObj = new Object();
    //private final Set<Long> processingSet = new HashSet<Long>();
    private final String shortName;

    public QueueDaemon(final TransactionTemplate txTemplate,
                       final TaskStorage<T> taskStorage, final TaskExecutor<T> executor,
                       final long scanPeriod, int threadCount, String shortName) {
        this.shortName = shortName;
        thrds = new Thread[threadCount];
        this.thrdRunnable = new Runnable() {

            void processTask(final T task) throws InterruptedException {
                boolean done = executor.processTask(task);
                synchronized (syncObj) {
                    if (done) {
                        try {
                            txTemplate.execute(new TransactionCallbackWithoutResult() {
                                @Override
                                protected void doInTransactionWithoutResult(TransactionStatus status) {
                                    taskStorage.deleteTask(task);
                                }
                            });
                        } catch (Exception e) {
                            LOG.error("Delete failed, retry made, " + e.getMessage());
                            Thread.sleep(200);
                            try {
                                txTemplate.execute(new TransactionCallbackWithoutResult() {
                                    @Override
                                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                                        taskStorage.deleteTask(task);
                                    }
                                });
                            } catch (Exception ee) {
                                LOG.error("Delete failed: " + ee.getMessage() + ", task " + task);
                            }
                        }
                    }
                }
            }

            @Override
            public void run() {
                final String threadName = "TaskFetch-" + Thread.currentThread().getName();
                try {
                    Thread.sleep(5000 + ThreadLocalRandom.current().nextInt(10000));
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted", e);
                    return;
                }

                for (; ; ) {
                    T task = null;
                    try {
                        Thread.sleep(scanPeriod);
                        boolean again = true;
                        while (again) {
                            LOG.info("Scanning queue...");
                            task = txTemplate.execute(status -> taskStorage.fetchNextTask());

                            if (task != null) {
                                try {
                                    LOG.debug("Processing: {}", task);
                                    processTask(task);
                                } catch (Exception e) {
                                    LOG.error("Process task: " + e.getMessage() + ", task " + task, e);
                                }
                                again = true;
                            } else {
                                again = false;
                            }

                            //throttle
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted", e);
                        return;
                    } catch (Exception e) {
                        LOG.error("Queue error (" + e.getClass().getName() + "): " + e.getMessage() + ", task " + task, e);
                    }
                }
            }


        };
    }

    public void init() {
        LOG.info("Starting...");
        for (int i = 0; i < thrds.length; i++) {
            thrds[i] = new Thread(thrdRunnable);
            thrds[i].setName("QD - " + shortName + " " + (i + 1));
            thrds[i].setDaemon(true);
            thrds[i].start();
        }
    }

    public void shutdown() {
        LOG.info("Stopping...");
        for (Thread t : thrds) {
            t.interrupt();
        }
        try {
            boolean someRunning = false;

            for (int i = 0; i < 10; i++) {
                someRunning = false;
                Thread.sleep(200);
                for (Thread t : thrds) {
                    someRunning = someRunning || t.isAlive();
                }
                if (!someRunning) {
                    return;
                }
            }
            if (someRunning) {
                LOG.error("Some threads running!");
            }
        } catch (InterruptedException e) {
            LOG.error("Interrupted", e);
        }
    }
}
