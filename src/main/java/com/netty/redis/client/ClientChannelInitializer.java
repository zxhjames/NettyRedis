package com.netty.redis.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @program: netty-4-user-guide-demos-master
 * @description:
 * @author: 占翔昊
 * @create 2022-04-28 19:45
 **/
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // 客户端定时向服务端发送心跳
        /**
         * 1.client启动一个定时器，不断发送心跳
         * 2.server收到心跳后，作出回应
         * 3.server启动一个定时器，判断client是否存在，通过时间差和简单标识进行判断
         */
        channel.pipeline().addLast(new IdleStateHandler(0,4, 0,TimeUnit.SECONDS));

        // 添加拦截器
        channel.pipeline().addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
        channel.pipeline().addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
        channel.pipeline().addLast(new ClientHandler());
    }
}
