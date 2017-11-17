package ru.netradar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.netradar.server.acceptor.sockets.connect.TRLocThread;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by rfk on 02.11.2017.
 */
@SpringBootTest(classes = {NetradarApplication.class})
public class NetradarApplicationTest extends AbstractTestNGSpringContextTests {
    private static final Logger LOG = LoggerFactory.getLogger(NetradarApplicationTest.class);

    @Autowired
    private Environment environment;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeClass
    public void setupBeforeClass() {
        LOG.info("setupBeforeClass()");
        System.out.println("server.port=" + environment.getProperty("server.port"));
        this.mockMvc = webAppContextSetup(this.wac).build();
        LOG.info("Setup OK");
    }

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    public void testAbout() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"));
    }

    @Test
    public void shouldAcceptTR102String() throws Exception {
        String s = "$355632000166323,1,1,040202,093633,E12129.2252,N2459.8891,00161,0.0100,147,07*37!\n";
        sendTR102TCPString(s);
        Thread.sleep(1000);
       // Assert.assertEquals(httpConsumer.getAcceptCount(), 3);
        Assert.assertEquals(TRLocThread.tryCount, 1);

    }

    @Test
    public void shouldAcceptTR102NewPortString() throws Exception {
        String s = "$1000000001,1,2,040202,093633,E12129.2252,N2459.8891,00161,0.0100,147,07*37!\n";
        sendAsyncTR102TCPString(s);
        Thread.sleep(1000);
        // Assert.assertEquals(httpConsumer.getAcceptCount(), 3);
       // Assert.assertEquals(TRLocThread.tryCount, 1);

    }

    private void sendTR102TCPString(String s) throws IOException, InterruptedException {
        int port = Integer.parseInt(environment.getProperty("acceptor.portTr102"));
        sendLocalhostTCPString(s, port);
    }

    private void sendAsyncTR102TCPString(String s) throws IOException, InterruptedException {
        int port = Integer.parseInt(environment.getProperty("acceptor.portAsyncTr102"));
        sendLocalhostTCPString(s, port);
    }

    public static void sendLocalhostTCPString(String s, int port) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", port);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(s.getBytes());
        outputStream.flush();
        Thread.sleep(1000);
        outputStream.close();
        socket.close();
    }
}
