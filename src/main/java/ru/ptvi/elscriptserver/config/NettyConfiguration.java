package ru.ptvi.elscriptserver.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ptvi.elscriptserver.tcpserver.ScriptServerChannelInitializer;

import java.net.InetSocketAddress;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(NettyProperties.class)
public class NettyConfiguration {

    private final NettyProperties nettyProperties;

    @Bean(name = "serverBootstrap")
    public ServerBootstrap bootstrap(ScriptServerChannelInitializer scriptServerChannelInitializer) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup())
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(scriptServerChannelInitializer);
        b.option(ChannelOption.SO_BACKLOG, nettyProperties.getBacklog());
        return b;
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(nettyProperties.getBossCount());
    }

    @Bean(destroyMethod = "shutdownGracefully")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(nettyProperties.getWorkerCount());
    }

    @Bean
    public InetSocketAddress tcpSocketAddress() {
        return new InetSocketAddress(nettyProperties.getTcpPort());
    }
}
