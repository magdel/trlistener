package ru.netradar.util;

import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 14.06.2011
 * Time: 23:08:48
 * To change this template use File | Settings | File Templates.
 */
public class MD5Test {
    @Test
    public void testEquals() {
        byte[] b1 = new byte[10];
        byte[] b2 = new byte[16];
        byte[] b3 = new byte[16];
        byte[] b4 = new byte[16];
        b1[1] = 10;
        b2[1] = 10;
        b3[1] = 10;
        b4[1] = 11;

        Assert.assertFalse(MD5.isEqualsHashs(b1, b2));
        Assert.assertTrue(MD5.isEqualsHashs(b2, b3));
        Assert.assertFalse(MD5.isEqualsHashs(b3, b4));
    }
}
