/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.netradar.server.diag;

/**
 * Интерфейс диагностики
 *
 * @author rfk
 */
public interface DiagInformation {
    public String getDiagName();

    public String getDiagStatus();

    public String getDiagDescription();
}
