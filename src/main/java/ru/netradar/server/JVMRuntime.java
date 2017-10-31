package ru.netradar.server;

import com.sun.management.OperatingSystemMXBean;
import org.apache.log4j.Logger;
import ru.netradar.util.Formats;

import java.lang.management.*;
import java.text.DecimalFormat;


public class JVMRuntime implements Runnable {

    private static final Logger log = Logger.getLogger(JVMRuntime.class);
    private OperatingSystemMXBean osbean;
    private RuntimeMXBean runtimebean;
    private MemoryMXBean membean;
    private ThreadMXBean thrbean;
    private static final DecimalFormat doubleFormat = new DecimalFormat("0.00");
    private static final long sleepInterval = 60000; // 60 sec default sleep interval
    private int threadCount = 0;
    //private final ConnectionPool pool;
    private Thread thrd;

    public JVMRuntime() {
    }

    public void init() {
        log.info("Starting JVM runtime stats collector...");
        osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        runtimebean = ManagementFactory.getRuntimeMXBean();
        membean = ManagementFactory.getMemoryMXBean();
        thrbean = ManagementFactory.getThreadMXBean();
        // start
        thrd = new Thread(this);
        thrd.setName("JVMRuntime stats collector");
        thrd.setDaemon(true);
        thrd.start();
    }

    public void shutdown() {
        log.info("Shutting down JVMRuntimeDaemon...");
        if (thrd != null) {
            thrd.interrupt();
            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(200);
                    if (thrd.isAlive()) {
                        log.info("JVMRuntimeDaemon running...");
                    } else {
                        log.info("JVMRuntimeDaemon finished...");
                        return;
                    }
                }
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    /**
     * JVM data for JMX
     */
    private long sysUptime = 0;
    private long sysCpuTime = 0;
    private double cpuUsage = 0;
    private long memUsed = 0;
    private long heapSize = 0;
    private long maxSize = 0;

    /**
     * MARS thread runner
     */
    public void run() {
        StringBuilder sb = new StringBuilder();
        long lastUpTime = runtimebean.getUptime();
        long lastCPUTime = (osbean.getProcessCpuTime() / osbean.getAvailableProcessors()) / 1000000;
        boolean memMaxDefined = membean.getHeapMemoryUsage().getMax() > 0;
        log.info("Maximum defined heap size -Xmx=" + membean.getHeapMemoryUsage().getMax() + " is defined:" + memMaxDefined);

        try {
            //noinspection InfiniteLoopStatement
            while (true) {

                sysUptime = runtimebean.getUptime();
                long cpuTime = (osbean.getProcessCpuTime() / osbean.getAvailableProcessors()) / 1000000;

                double upDelta;
                if (sysUptime > lastUpTime) {
                    upDelta = sysUptime - lastUpTime;
                } else {
                    upDelta = sysUptime;
                }

                double cpuDelta;
                if (cpuTime > lastCPUTime) {
                    cpuDelta = cpuTime - lastCPUTime;
                } else {
                    cpuDelta = cpuTime;
                }


                lastUpTime = sysUptime;
                lastCPUTime = cpuTime;

                sysCpuTime = cpuTime;
                cpuUsage = (100 * cpuDelta / upDelta);

                MemoryUsage usage = membean.getHeapMemoryUsage();
                memUsed = usage.getUsed() / 1024;
                heapSize = usage.getCommitted() / 1024;
                maxSize = (memMaxDefined ? usage.getMax() : usage.getCommitted()) / 1024;
                double memUsage = ((double) memUsed / (double) maxSize) * 100;

                threadCount = thrbean.getThreadCount();

                // dump to logs
                sb.setLength(0);
                sb.append(" CPU:").append(doubleFormat.format(cpuUsage)).
                        append("% Heap:").append(doubleFormat.format(memUsage)).
                        append(" } Heap={ Used:").append(memUsed).
                        append("Kb Size:").append(heapSize).
                        append("Kb Max:").append(maxSize).
                        append("Kb } Thread count:").append(threadCount).
                    //    append(" DBconnpool={ Used:").append(pool.getUsedConnections()).
                    //    append(" Free:").append(pool.getFreeConnections()).
                    //    append(" Max:").append(pool.getMaxLimit()).
                        append(" } Uptime: ").append(Formats.formatTimePeriod(sysUptime));
                log.info(sb);
                Thread.sleep(sleepInterval);
            }
        } catch (InterruptedException ie) {
            log.warn("JVMRuntime is interrupted");
        }
    }

    public long getIUptime() {
        return sysUptime;
    }

    public long getICPUtime() {
        return sysCpuTime;
    }

    public double getCPU() {
        return cpuUsage;
    }

    public int getIHeapUsed() {
        return (int) memUsed;
    }

    public int getIHeapSize() {
        return (int) heapSize;
    }

    public int getIHeapMax() {
        return (int) maxSize;
    }

    public int getThreadCount() {
        return threadCount;
    }

}
