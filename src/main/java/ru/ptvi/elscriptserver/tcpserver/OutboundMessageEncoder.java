package ru.ptvi.elscriptserver.tcpserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.springframework.stereotype.Component;
import ru.ptvi.elscriptserver.tcpserver.message.ChunkResponseMessage;

@Component
@ChannelHandler.Sharable
public class OutboundMessageEncoder extends MessageToByteEncoder<ChunkResponseMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ChunkResponseMessage msg, ByteBuf out) {
        // Заголовок
        out.writeInt(ProtocolUtils.HEADER_MAGIC_SIGNATURE);
        ProtocolUtils.writeZeroTermStringFixedLength(out, msg.scriptName(), 70);
        out.writeIntLE(msg.chunkNumber());
        out.writeShortLE(msg.content().length);

        // Данные пакета
        out.writeBytes(msg.content());

        // Crc
        ByteBuf duplicate = out.slice();
        int crc = ProtocolUtils.calcCrc(duplicate, msg.content().length + 80 /*header size*/);
        out.writeShortLE(crc);
    }
}
