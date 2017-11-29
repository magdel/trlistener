package ru.netradar.server.port.tr102;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.netradar.server.port.ConnectionData;
import ru.netradar.server.port.ConnectionId;
import ru.netradar.utils.SimpleFlakeIdGenerator;

import java.util.Optional;

/**
 * Created by rfk on 16.11.2017.
 */
public class StringToNrLocationRecordMapperTest {
    @Test
    public void testApply() throws Exception {
        StringToNrLocationRecordMapper mapper = new StringToNrLocationRecordMapper(new SimpleFlakeIdGenerator());
        Optional<Tr102Message> messageOptional = mapper.apply(
                new ConnectionData<>(
                        new ConnectionId(1L),
                        null,
                        "$355632000166323,1,2,040202,093633,E12129.2252,N2459.8891,00161,0.0100,147,07*37!"));
        Assert.assertTrue(messageOptional.isPresent());
        Tr102Message message = messageOptional.get();
        Assert.assertEquals(message.getTr102Iden().getImei(), "355632000166323");
        Assert.assertEquals(message.getNrLocation().crs(), (short) 147);
    }

}