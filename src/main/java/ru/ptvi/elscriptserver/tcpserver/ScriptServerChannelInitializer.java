package ru.ptvi.elscriptserver.tcpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ptvi.elscriptserver.config.NettyProperties;

@Component
@RequiredArgsConstructor
public class ScriptServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final NettyProperties nettyProperties;

    private final InboundMessageDecoder inboundMessageDecoder;

    private final InboundMessageHandler inboundMessageHandler;

    private final InboundExceptionHandler inboundExceptionHandler;

    private final OutboundMessageEncoder outboundMessageEncoder;

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // /////////////////////////////////////
        // Хендлеры входящих сообщений

        pipeline.addLast(new ReadTimeoutHandler(nettyProperties.getReadTimeout()));

        // Запрос от терминала к серверу фиксированной длины - 95 байт
        pipeline.addLast(new FixedLengthFrameDecoder(95));
        pipeline.addLast(inboundMessageDecoder);
        pipeline.addLast(inboundMessageHandler);
        pipeline.addLast(inboundExceptionHandler);

        // /////////////////////////////////////
        // Хендлеры исходящих сообщений, просматриваются снизу вверх

        // Терминал не требует финального пакета, хоть он и присутствует в описании протокола.
        // Пока что не будем его слать. Настройка кодирования финального пакета выглядела бы так:
        //  pipeline.addLast(new OutboundFinalMessageEncoder());

        pipeline.addLast(outboundMessageEncoder);
    }
}

