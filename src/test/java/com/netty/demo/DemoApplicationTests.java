package com.netty.demo;

import com.netty.redis.client.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class TestClient {
    private final String host;
    private final int port;

    public TestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }



    public void boost() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(bossGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast("decoder",new StringDecoder(CharsetUtil.UTF_8));
                            channel.pipeline().addLast("encoder",new StringEncoder(CharsetUtil.UTF_8));
                            channel.pipeline().addLast(new ClientHandler());
                        }
                    });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            while (future.channel().isActive()) {
                future.channel().writeAndFlush("heartBeat");
                Thread.sleep(6000);
            }
        }finally {
            bossGroup.shutdownGracefully();
        }
    }
    @Test
    public static void main(String[] args)throws Exception {
        new TestClient("127.0.0.1",8080).boost();
    }


}
