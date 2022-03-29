package ru.ptvi.elscriptserver.tcpserver;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.ptvi.elscriptserver.domain.Script;
import ru.ptvi.elscriptserver.domain.ScriptChunk;
import ru.ptvi.elscriptserver.event.ChannelClosed;
import ru.ptvi.elscriptserver.event.ChannelOpened;
import ru.ptvi.elscriptserver.event.ChunkRequested;
import ru.ptvi.elscriptserver.service.ScriptService;
import ru.ptvi.elscriptserver.tcpserver.message.ChunkRequestMessage;
import ru.ptvi.elscriptserver.tcpserver.message.ChunkResponseMessage;

@Slf4j
@AllArgsConstructor
@Component
@ChannelHandler.Sharable
public class InboundMessageHandler extends SimpleChannelInboundHandler<ChunkRequestMessage> {
    private final ScriptService scriptService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        eventPublisher.publishEvent(new ChannelOpened(ctx.channel().id().asLongText()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        eventPublisher.publishEvent(new ChannelClosed(ctx.channel().id().asLongText()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChunkRequestMessage msg) throws Exception {
        log.info("{} requests chunk #{} of script '{}'",
                msg.equipmentId(),
                msg.chunkNumber(),
                msg.scriptName());

        Script script = scriptService.getByName(msg.scriptName());
        ScriptChunk chunk = scriptService.getChunk(script, msg.chunkNumber());
        eventPublisher.publishEvent(new ChunkRequested(ctx.channel().id().asLongText(),
                msg.equipmentId(),
                script,
                msg.chunkNumber()));
        ChunkResponseMessage response = new ChunkResponseMessage(msg.scriptName(), msg.chunkNumber(), chunk.data());

        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (chunk.isLast())
            channelFuture.addListener(f -> ctx.close());

        // здесь самое место чтоб отправить финальное сообщение, если передали последний кусок,
        // но терминал не ребует финального сообщения
        // ctx.channel().writeAndFlush(new FinalMessage(script.script().name(), 0x3939, 0x3737))
        //      .addListener(f -> ctx.close());;
    }
}
