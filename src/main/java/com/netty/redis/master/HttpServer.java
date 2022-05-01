package com.netty.redis.master;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * @program: netty-4-user-guide-demos-master
 * @description:
 * @author: 占翔昊
 * @create 2022-04-28 14:02
 **/
@Component("masterNettyServer")
public class HttpServer {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(); //NioEventLoopGroup extends MultithreadEventLoopGroup Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private Logger logger = LoggerFactory.getLogger(HttpServer.class);
    public ChannelFuture bind(InetSocketAddress address) {
        ChannelFuture future = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 500)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new NettyServerChannelInitializer());

            // 绑定端口，开始接收进来的连接
            future = bootstrap.bind(address).sync();
            //关闭channel和块，直到它被关闭
            channel = future.channel();
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (null != future && future.isSuccess()) {
                logger.info("master服务启动>>>>>>>>>>>>>>>");
            } else {
                logger.error("master服务出错>>>>>>>>>>>>>>>");
            }
        }
        return future;
    }


    public void destroy() {
        if (null == channel) return;
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public Channel getChannel() {
        return channel;
    }
}
