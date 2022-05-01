package com.netty.redis.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @program: netty-4-user-guide-demos-master
 * @description:
 * @author: 占翔昊
 * @create 2022-04-28 14:22
 **/
@Component
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private ClientServer clientServer = new ClientServer();
    @Value("${server.master.port}")
    private int masterPort = 8080;

    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat", CharsetUtil.UTF_8));
    /** 空闲次数 */
    private int idle_count = 1;

    /** 发送次数 */
    private int count = 1;

    /**循环次数 */
    private int fcount = 1;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("激活的时间是: " + new Date());
        System.out.println("连接激活");
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("停止时间是: " + new Date());
        System.out.println("关闭连接");
        clientServer.Recovery(masterPort);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if (idle_count <= 3) {
                    idle_count++;
                    ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                } else {
                    System.out.println("不再发送心跳");
                }
            }
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }


}
