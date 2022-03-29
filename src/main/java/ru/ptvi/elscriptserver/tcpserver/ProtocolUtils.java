package ru.ptvi.elscriptserver.tcpserver;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ProtocolUtils {
    private ProtocolUtils() {
    }

    final static int HEADER_MAGIC_SIGNATURE = 0x41A41221;
    private final static Charset CHARSET = Charset.forName("Windows-1251");
    private final static byte[] ZERO_BYTES_ARRAY = new byte[100];

    static String readZeroTermStringFixedLength(ByteBuf in, int fixedLength) {
        int posOfZero = in.indexOf(in.readerIndex(), in.readerIndex() + fixedLength, (byte)0);
        int len = posOfZero < 0 ? fixedLength : posOfZero - in.readerIndex();

        byte[] buf = new byte[len];
        in.readBytes(buf);
        if (buf.length < fixedLength)
            in.skipBytes(fixedLength - buf.length);

        return new String(buf, CHARSET);
    }

    static void writeZeroTermStringFixedLength(ByteBuf out, String str, int fixedLength) {
        byte[] bytesOfStr = str.getBytes(CHARSET);
        if (bytesOfStr.length > fixedLength)
            out.writeBytes(Arrays.copyOf(bytesOfStr, fixedLength));
        else {
            out.writeBytes(bytesOfStr);
            out.writeBytes(ZERO_BYTES_ARRAY,0, fixedLength - bytesOfStr.length);
        }
    }

    static int calcCrc(ByteBuf buf, int len) {
        long crc = 0xFFFF;
        for (int i = 0; i < len; i++) {
            crc ^= buf.getUnsignedByte(i) & 0xFF;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc = crc >> 1;
                }
            }
        }
        crc = crc & 0xFFFF;
        return (int)crc;
    }
}
