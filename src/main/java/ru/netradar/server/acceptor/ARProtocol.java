/*
 * TRProtocol.java
 *
 * Created on 19 02 2008 , 18:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.netradar.server.acceptor;

/**
 * @author Raev
 */
public class ARProtocol {

    public final static byte STATUS_WAITAUTHORIZATION = 1;
    public final static byte STATUS_WAITCOORDINATES = 2;

    public final static byte COMMAND_HEADERSIZE = 3;
    public final static byte COMMAND_SIZE = 2;

    public final static byte COMMAND_AUTHORIZE = 1;
    public final static byte COMMAND_COORDINATES = 2;
    public final static byte COMMAND_PING = 3;
    public final static byte COMMAND_REPORT = 4;

    public final static byte COMMAND_REPORT_WITH_DATE = 5;
    public final static byte COMMAND_DATE_COORDINATES = 6;

    public static String[] COMMANDS_NAMES = {"AUTH", "COOR", "PING", "REPO", "DTREPO", "DTCOORD"};
}
