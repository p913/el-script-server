package ru.ptvi.elscriptserver.tcpserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolUtilsTest {
    @Test
    void crcAlgorithmTest() {
        ByteBuf packet = Unpooled.buffer(95);
        packet.writeBytes(new byte[]  {0x41, (byte)0xA4, 0x12, 0x21, 0x38, 0x36, 0x36, 0x35, 0x35, 0x37, 0x30, 0x35, 0x35, 0x39, 0x35, 0x34,
                0x38, 0x34, 0x38, (byte)0xCE, (byte)0xCE, (byte)0xCE, (byte)0x5F, (byte)0xC0, (byte)0xEB, (byte)0xF2, (byte)0xE0, (byte)0xE8, (byte)0xF0, 0x2F, 0x6D, 0x79,
                0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x2E, 0x62, 0x69, 0x6E, 0x00, 0x00, 0x55, 0x73, 0x00, 0x2C,
                0x39, 0x3D, 0x65, 0x73, 0x70, 0x67, 0x20, 0x53, 0x67, 0x76, 0x41, 0x32, 0x3D, 0x72, 0x6E, 0x67,
                0x20, 0x2C, 0x35, 0x61, 0x4D, 0x73, 0x70, 0x72, 0x6E, 0x53, 0x78, 0x2C, 0x34, 0x33, 0x3D, 0x6E,
                0x6C, 0x67, 0x20, 0x73, 0x69, 0x56, 0x73, 0x65, 0x6C, 0x00, 0x00, 0x00, 0x00});

        int crc = ProtocolUtils.calcCrc(packet, 93);

        assertEquals(0x2136, crc);
    }

    @Test
    void shouldReadCyrillicScriptNameThenEncodedIn1251CodePageWithEndingZeroes() {
        ByteBuf cyrillicScriptName = Unpooled.buffer(95);
        cyrillicScriptName.writeBytes(new byte[]  {(byte)0xCE, (byte)0xCE, (byte)0xCE,
                (byte)0xC3, (byte)0xEB, (byte)0xEE, (byte)0xED, (byte)0xE0, (byte)0xF1, (byte)0xF1, 0x2F, 0x6D, 0x79,
                0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x2E, 0x62, 0x69, 0x6E, 0x00, 0x00, 0x55, 0x73, 0x00, 0x2C,
                0x39, 0x3D, 0x65, 0x73, 0x70, 0x67, 0x20, 0x53, 0x67, 0x76, 0x41, 0x32, 0x3D, 0x72, 0x6E, 0x67,
                0x20, 0x2C, 0x35, 0x61, 0x4D, 0x73, 0x70, 0x72, 0x6E, 0x53, 0x78, 0x2C, 0x34, 0x33, 0x3D, 0x6E,
                0x6C, 0x67, 0x20, 0x73, 0x69, 0x56, 0x73, 0x65, 0x6C, 0x00, 0x00, 0x00, 0x00});

        String scriptName = ProtocolUtils.readZeroTermStringFixedLength(cyrillicScriptName, 70);

        assertEquals("ОООГлонасс/myscript.bin", scriptName);
        assertEquals(70, cyrillicScriptName.readerIndex());
    }

    @Test
    void shouldReadFullPayloadThenNoEndingZeroes() {
        ByteBuf buf = Unpooled.buffer(95);
        buf.writeBytes("866557055954848".getBytes());

        String equipmentId = ProtocolUtils.readZeroTermStringFixedLength(buf, 15);

        assertEquals("866557055954848", equipmentId);
        assertEquals(15, buf.readerIndex());
    }

}