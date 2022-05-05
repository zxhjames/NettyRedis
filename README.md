# 工程简介

使用netty原生框架写Redis服务


# 模块架构
![https://img-blog.csdnimg.cn/153cf7de8f8c47ac86cc18cc92289f25.png](https://img-blog.csdnimg.cn/153cf7de8f8c47ac86cc18cc92289f25.png)

## Server端

Sever端负责解析TCP连接与websocket请求，并转发给相应的handler进行业务处理，同时手动设计了一个基于concurrentHashMap的缓存策略以及aof定时写日志功能，详细请见代码注释

## Client端

Client端用来接受Server端发送过来的数据，并加入了断线重连和心跳检测功能
