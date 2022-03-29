package ru.ptvi.elscriptserver.tcpserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ptvi.elscriptserver.tcpserver.message.FinalMessage;

@Slf4j
@Component
@ChannelHandler.Sharable
public class OutboundFinalMessageEncoder extends MessageToByteEncoder<FinalMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, FinalMessage msg, ByteBuf out) {
        // Заголовок
        out.writeInt(ProtocolUtils.HEADER_MAGIC_SIGNATURE);
        ProtocolUtils.writeZeroTermStringFixedLength(out, msg.scriptName(), 70);
        out.writeIntLE(0);
        out.writeShortLE(0);

        // Crc всего скрипта
        out.writeIntLE(msg.scriptDataCrc());

        // Размер всего скрипта в байтах
        out.writeIntLE(msg.scriptSize());

        // Crc
        ByteBuf duplicate = out.slice();
        int crc = ProtocolUtils.calcCrc(duplicate, 8 + 80 /*crc32 + length32 + header size*/);
        out.writeShortLE(crc);
    }
}
