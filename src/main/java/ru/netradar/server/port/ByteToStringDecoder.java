package ru.netradar.server.port;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by rfk on 08.11.2017.
 */
public class ByteToStringDecoder extends ByteToMessageDecoder { // (1)
    private static final Logger log = LoggerFactory.getLogger(ByteToStringDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        log.info("Come data: bytes={}", in.readableBytes());
        ByteBuf byteBuf = in.readBytes(in.readableBytes());
        byte[] array = byteBuf.array();
        byteBuf.release();
        out.add(new String(array, StandardCharsets.UTF_8)); // (4)
    }
}
