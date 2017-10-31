/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server.diag;

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * @author rfk
 */
public class DiagStatus {

    private static String STAT_OK_DESC = "Нормальная работа на ";
    private static String STAT_BAD_DESC = "Сбой в ";
    private static String STAT_LAST_ERR = "\nПоследняя ошибка в ";
    private static String STAT_NO_INFO = "Нет информации о работе сервиса на ";
    private static String STATUS_OK = "OK";
    private static String STATUS_WARN = "WARN";
    private static String STATUS_BAD = "BAD";
    private long tmCreated = System.currentTimeMillis();
    private long tmUpdated;
    private long tmError;
    private String statusName = STATUS_OK;
    //private String lastErrorDesc="";
    /**
     * Один час предупреждаем
     */
    private static long TIME2WARN = 1000 * 60 * 60;
    private DateFormat dtFormater = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

    public String getStatusName() {
        return statusName;
    }

    public String getStatusDesc() {
        String res;
        if (tmUpdated > 0) {
            if (statusName.equals(STATUS_BAD)) {
                res = STAT_BAD_DESC + dtFormater.format(tmUpdated) + " \n" + getErrorLog();
            } else {
                if (tmError > 0) {
                    res = STAT_OK_DESC + dtFormater.format(tmUpdated) + ":\n" + getErrorLog();
                } else {
                    res = STAT_OK_DESC + dtFormater.format(tmUpdated);
                }
            }
        } else {
            res = STAT_NO_INFO + dtFormater.format(System.currentTimeMillis());
        }
        return res + "\n \nСтатистика по сервису работает с " + dtFormater.format(tmCreated);
    }

    public void updateStatusOK() {
        tmUpdated = System.currentTimeMillis();
        if (tmError < System.currentTimeMillis() - TIME2WARN) {
            statusName = STATUS_OK;
        } else {
            statusName = STATUS_WARN;
        }
    }

    private static int MAX_ERROR_LOG = 20;
    private ArrayList<String> lastErrorsList = new ArrayList(MAX_ERROR_LOG);

    private void appendErrorToList(String errorText) {
        if (lastErrorsList.size() >= MAX_ERROR_LOG) {
            lastErrorsList.remove(MAX_ERROR_LOG - 1);
        }
        lastErrorsList.add(0, errorText);
        lastErrorLogChanged = true;
    }

    private String lastErrorLog;
    private boolean lastErrorLogChanged = true;

    private String getErrorLog() {
        String res = "";
        for (int i = 0; i < lastErrorsList.size(); i++) {
            res += lastErrorsList.get(i);
        }
        lastErrorLog = res;
        lastErrorLogChanged = false;
        return lastErrorLog;
    }

    public void updateStatusBAD(String errorDescription) {
        tmUpdated = System.currentTimeMillis();
        tmError = System.currentTimeMillis();
        appendErrorToList("\n-----\n" + dtFormater.format(tmError) + ": " + errorDescription + "\n");
        statusName = STATUS_BAD;
    }
}
