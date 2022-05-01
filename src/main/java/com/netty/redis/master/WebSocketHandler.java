package com.netty.redis.master;

import com.netty.redis.service.CacheService;
import com.netty.redis.service.ScheduledService;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @program: netty-4-user-guide-demos-master
 * @description:
 * @author: 占翔昊
 * @create 2022-04-28 14:04
 **/
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private static ConcurrentHashMap<String, Channel> map = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private CacheService cacheService = new CacheService();
    private ScheduledService scheduledService = new ScheduledService();
    private Integer bgSaveTime = 3; // 备份时间
    private ByteBuf buffer = Unpooled.directBuffer(); // 写文件缓存
    private String msg = new String();

    /**空闲次数**/
    private int idle_count = 1;

    /**发送次数**/
    private int count = 1;

    // 服务端心跳检测器
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("接受消息超时");
                if (idle_count > 2) {
                    System.out.println("关闭不活跃的连接");
                    ctx.channel().close();
                }
                idle_count++;
            }
        } else {
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        if (object instanceof TextWebSocketFrame){
            // 如果是websocket连接
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame)object;
            msg = textWebSocketFrame.text();
            cacheService.saveCache(ctx.channel().remoteAddress().toString(), msg);
            buffer.writeBytes((System.currentTimeMillis() + " PUT").getBytes()).writeBytes(ctx.channel().remoteAddress().toString().getBytes())
                    .writeBytes(" ".getBytes())
                    .writeBytes(msg.getBytes());
            // todo bgSave线程静默写入日志
            ctx.channel().eventLoop().schedule(() -> {
                scheduledService.bgSave(buffer);
                scheduledService.distributeSend(ctx,msg,map);
            },bgSaveTime, TimeUnit.SECONDS);
        }
        else {
            String message = (String) object;
            if ("heartBeat".equals(message)) {  //如果是心跳命令，则发送给客户端;否则什么都不做
                ctx.writeAndFlush("服务端成功收到心跳信息");
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded::"+ctx.channel().id().asLongText());
        InetSocketAddress socketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
        String hostAddress = socketAddress.getAddress().getHostAddress();
        logger.debug("IP:{}",hostAddress);
        String clientId = ctx.channel().id().toString();
        map.put(clientId,ctx.channel());
        logger.debug("map:{}",map.toString());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved::"+ctx.channel().id().asLongText());
        String clientId = ctx.channel().id().toString();
        map.remove(clientId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught::"+cause.getMessage());
        String clientId = ctx.channel().id().toString();
        map.remove(clientId);
        ctx.close();
    }
}
