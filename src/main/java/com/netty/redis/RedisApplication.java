package com.netty.redis;

import com.netty.redis.client.ClientServer;
import com.netty.redis.master.HttpServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RedisApplication implements CommandLineRunner {
    @Autowired
    private HttpServer httpServer;
    @Autowired
    private ClientServer clientServer;
    @Value("${server.host}")
    private String host;
    @Value("${server.port}")
    private int port;
    @Value("${server.mode}")
    private int mode;
    @Value("${server.master.port}")
    private int masterPort;
    public ChannelFuture channelFuture;
    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (mode == 0) {
            System.out.println("启动master>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            InetSocketAddress address = new InetSocketAddress(host, port);
            channelFuture = httpServer.bind(address);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> httpServer.destroy()));
            channelFuture.channel().closeFuture().syncUninterruptibly();
        } else {
            System.out.println("启动slave>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            InetSocketAddress address = new InetSocketAddress(host, port);
            channelFuture = clientServer.bind(masterPort);
            while (channelFuture.channel().isActive()) {
                channelFuture.channel().writeAndFlush("heartBeat");
                Thread.sleep(6000);
            }
            Runtime.getRuntime().addShutdownHook(new Thread(() -> clientServer.destroy()));
            channelFuture.channel().closeFuture().syncUninterruptibly();
        }

    }
}
