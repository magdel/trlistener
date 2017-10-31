/*
 * NRObject.java
 *
 * Created on 19  15:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.netradar.server.device;

/**
 * Base class for all tracking objects
 *
 * @author Raev
 */
public class NRObject {
    /**
     * ID of user
     */
    public int userId = -1;
    /**
     * Object name
     */
    public String name;
    /* Tracking object type
   MAPNAVUSERTYPE =1;
   ARTALUSERTYPE =2;
   TR102USERTYPE =3;
    */
    public byte userType;

    private byte[] passwordMD5HashBytes;
    private String passwordMD5Hash = "";


    /**
     * Serial for artals, for debug purposes
     */
    public int sn;
    /**
     * PIN for artals, for debug purposes
     */
    public short pin;

    /**
     * Creates a new instance of NRObject, pass ID and TYPE
     */
    public NRObject(int id, byte userType) {
        this.userType = userType;
        this.userId = id;
    }


    /**
     * MAPNAVUSERTYPE =1 - MapNav user
     */
    public final static byte MAPNAVUSERTYPE = 1;
    /**
     * ARTALUSERTYPE =2 - Artal
     */
    public final static byte ARTALUSERTYPE = 2;
    /**
     * TR102USERTYPE =3 - TR-102
     */
    public final static byte TR102USERTYPE = 3;

    public void setPasswordMD5Hash(String passwordMD5Hash) {
        this.passwordMD5Hash = passwordMD5Hash == null ? "" : passwordMD5Hash;
        this.passwordMD5HashBytes = null;
    }

    public String getPasswordMD5Hash() {
        return passwordMD5Hash;
    }

    public byte[] getPasswordMD5HashBytes() {
        if (passwordMD5HashBytes != null) {
            return passwordMD5HashBytes;
        }
        if (passwordMD5Hash == null) {
            return null;
        }
        byte[] bytes = new byte[passwordMD5Hash.length() / 2];
        for (int i = 0; i < passwordMD5Hash.length() / 2; i++) {
            char a = passwordMD5Hash.charAt(i * 2);
            char b = passwordMD5Hash.charAt(i * 2 + 1);
            if (a > '9') {
                a = (char) (a - 'a' + 10);
            } else {
                a = (char) (a - '0');
            }
            if (b > '9') {
                b = (char) (b - 'a' + 10);
            } else {
                b = (char) (b - '0');
            }
            byte bt = (byte) ((a << 4) + b);
            bytes[i] = bt;
        }
        passwordMD5HashBytes = bytes;
        return passwordMD5HashBytes;
    }

    public final int hashCode() {
        return userId ^ userType;
    }

    public final boolean equals(Object obj) {
        NRObject rt = (NRObject) obj;
        return (rt.userId == userId) && (rt.userType == userType);
    }

    @Override
    public String toString() {
        return "NRObject{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", userType=" + userType +
                ", sn=" + sn +
                '}';
    }
}
