package com.example.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import org.springframework.stereotype.Component;

/**
 * @date : 2019/11/21
 */
@Component
public class NettyServer {

    private int port;

    public NettyServer() {
    }

    public NettyServer(int port) {
        this.port = port;
    }


    public void start(){
        // 主线程池
        NioEventLoopGroup bossGroup  = new NioEventLoopGroup();
        // 从线程池
        NioEventLoopGroup workerGroup  = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup ,workerGroup )
                //配置客户端的channel类型
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //配置TCP参数，握手字符串长度设置
                .option(ChannelOption.SO_BACKLOG, 1024)
                //TCP_NODELAY算法，尽可能发送大块数据，减少充斥的小块数据
                .option(ChannelOption.TCP_NODELAY, true)
                //开启心跳包活机制，就是客户端、服务端建立连接处于ESTABLISHED状态，超过2小时没有交流，机制会被启动
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //配置固定长度接收缓存区分配器
                .childOption(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(592048))
                //绑定I/O事件的处理类,WebSocketChildChannelHandler中定义
                .childHandler(new WebSocketServerInitializer());

            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind(port).sync();
            // 等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            Future<?> bossGroupFuture = bossGroup .shutdownGracefully();
            Future<?> workerGroupFuture = workerGroup .shutdownGracefully();
            try {
                bossGroupFuture.await();
                workerGroupFuture.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 描述：关闭Netty Websocket服务器，主要是释放连接
     *     连接包括：服务器连接serverChannel，
     *     客户端TCP处理连接bossGroup，
     *     客户端I/O操作连接workerGroup
     *
     *     若只使用
     *         bossGroupFuture = bossGroup.shutdownGracefully();
     *         workerGroupFuture = workerGroup.shutdownGracefully();
     *     会造成内存泄漏。
     */

}
