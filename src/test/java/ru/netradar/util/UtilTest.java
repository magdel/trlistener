package ru.netradar.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by rfk on 07.11.2017.
 */
public class UtilTest {
    @Test
    public void testGetHTTPDateTimeString() throws Exception {
        String httpDateTimeString = Util.getDateTimeStringInMsk();
        Assert.assertEquals(httpDateTimeString.length(), 16);
    }

}