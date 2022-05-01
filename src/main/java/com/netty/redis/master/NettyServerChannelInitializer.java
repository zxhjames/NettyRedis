package com.netty.redis.master;
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
 * @create 2022-04-28 14:02
 **/
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // 服务端挂载心跳检测路由
        channel.pipeline().addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS));

        // 添加一个socket网关路由，用来过滤不同的TCP请求
        channel.pipeline().addLast("gateway",new GateWayHandler());
        channel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
        channel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
        // 挂载websocket路由
        channel.pipeline().addLast("websocket",new WebSocketHandler());
    }
}
