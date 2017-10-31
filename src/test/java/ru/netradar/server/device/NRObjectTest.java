package ru.netradar.server.device;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.netradar.util.MD5;

/**
 * Created with IntelliJ IDEA.
 * User: rfk
 * Date: 19.03.13
 * Time: 1:19
 * To change this template use File | Settings | File Templates.
 */
public class NRObjectTest {
    @Test
    public void testMD5() {
        NRObject nro = new NRObject(1, (byte) 1);
        final String hashV = "ae3401bced0889ff00a0de469aeffe12";
        nro.setPasswordMD5Hash(hashV);
        final byte[] passwordMD5HashBytes = nro.getPasswordMD5HashBytes();
        final String s = MD5.toHexString(passwordMD5HashBytes);
        Assert.assertEquals(s, hashV);
    }

}
