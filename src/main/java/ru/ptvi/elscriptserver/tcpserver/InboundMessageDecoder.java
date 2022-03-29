package ru.ptvi.elscriptserver.tcpserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.springframework.stereotype.Component;
import ru.ptvi.elscriptserver.tcpserver.message.ChunkRequestMessage;

import java.net.ProtocolException;
import java.util.List;

@Component
@ChannelHandler.Sharable
public class InboundMessageDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (!msg.isReadable(95))
            throw new ProtocolException("Required 95 bytes for decode chunk request");

        int calculatedCrc = ProtocolUtils.calcCrc(msg, 93);

        if (msg.readInt() != ProtocolUtils.HEADER_MAGIC_SIGNATURE)
            throw new ProtocolException("Required 'magic' bytes in begin of request");

        ChunkRequestMessage chunkRequestMessage = new ChunkRequestMessage(
                ProtocolUtils.readZeroTermStringFixedLength(msg, 15),
                ProtocolUtils.readZeroTermStringFixedLength(msg, 70),
                msg.readIntLE());

        int readCrc = msg.readUnsignedShortLE();
        if (calculatedCrc != readCrc)
            throw new ProtocolException(String.format("Crc fail. Calculate 0x%04x, but read 0x%04x",
                    calculatedCrc, readCrc));

        out.add(chunkRequestMessage);
    }
}
