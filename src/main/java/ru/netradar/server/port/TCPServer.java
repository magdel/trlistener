package ru.netradar.server.port;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by rfk on 15.11.2016.
 */
public class TCPServer {
    private static final Logger logger = LoggerFactory.getLogger(TCPServer.class);

    private final int port;
    private final ChannelHandlerAdapter[] channelHandlerAdapters;
    private final ServerLoopGroup serverLoopGroup;
    private final boolean linebasedDelimiter;
    private ChannelFuture channelFuture;

    public TCPServer(int port,
                     ServerLoopGroup serverLoopGroup,
                     boolean linebasedDelimiter,
                     ChannelHandlerAdapter... channelHandlerAdapters) {
        this.port = port;
        this.channelHandlerAdapters = channelHandlerAdapters;
        this.serverLoopGroup = serverLoopGroup;
        this.linebasedDelimiter = linebasedDelimiter;
    }

    public void init() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(serverLoopGroup.getBossGroup(), serverLoopGroup.getWorkerGroup())
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            if (linebasedDelimiter) {
                                ch.pipeline()
                                        .addLast(new LineBasedFrameDecoder(1024));
                            }
                            ch.pipeline()
                                    .addLast(channelHandlerAdapters);
                        }
                    })
                    .option(ChannelOption.SO_LINGER, 0)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            channelFuture = serverBootstrap.bind(port);
            logger.info("Started TCP server: port={}", port);
        } catch (Exception e) {
            logger.error("Not started: port={}", port);
            throw new RuntimeException("TCP server failed to start", e);
        }
    }

    public void shutdown() {
        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        logger.info("Shutdowned TCP server: port={}", port);
    }


}
