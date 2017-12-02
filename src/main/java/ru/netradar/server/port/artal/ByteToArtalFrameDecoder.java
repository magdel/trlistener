package ru.netradar.server.port.artal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by rfk on 08.11.2017.
 */
public class ByteToArtalFrameDecoder extends ByteToMessageDecoder { // (1)
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)
        ByteBuf byteBuf = in.readBytes(in.readableBytes());
        byte[] array = byteBuf.array();
        byteBuf.release();
        out.add(new String(array, StandardCharsets.UTF_8)); // (4)
    }
}
