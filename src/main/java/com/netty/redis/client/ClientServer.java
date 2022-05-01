package com.netty.redis.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: netty-4-user-guide-demos-master
 * @description:
 * @author: 占翔昊
 * @create 2022-04-28 19:33
 **/
@Component
public class ClientServer {
    private Logger logger = LoggerFactory.getLogger(ClientServer.class);
    private final EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel = null;
    private Bootstrap bootstrap = null;
    private final int RECOVERY_WAIT_TIME = 4;


    public ChannelFuture bind(int masterPort) {
        ChannelFuture channelFuture = null;
        bootstrap =  new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new ClientChannelInitializer());
        try {
            channelFuture = bootstrap.connect("127.0.0.1",masterPort).sync();
            channel = channelFuture.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (channelFuture != null && channelFuture.isSuccess()) {
                logger.info("slave服务启动>>>>>>>>>>>>");
            } else {
                logger.error("slave服务出错>>>>>>>>>>>.");
            }
        }

        return channelFuture;
    }

    // client断线重连
    protected void Recovery(int masterPort) {
        bootstrap =  new Bootstrap();
        ChannelFuture future = bind(masterPort);
        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                logger.info("slaver重启成功");
            } else {
                logger.info("slaver服务器重连失败，尝试在重新连接.....");
                channelFuture.channel().eventLoop().schedule(new Runnable() {
                    @Override
                    public void run() {
                        Recovery(masterPort);
                    }
                },RECOVERY_WAIT_TIME,TimeUnit.SECONDS);
            }
        });
    }

    // 销毁客户端连接
    public void destroy() {
        if (channel == null) return;
        channel.close();
        group.shutdownGracefully();
    }

}
