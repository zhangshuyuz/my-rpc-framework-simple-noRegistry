package com.yuu.rpc.simple.netty.server;


import com.yuu.rpc.simple.netty.codec.NettyMessageDecoder;
import com.yuu.rpc.simple.netty.codec.NettyMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    /**
     * 对外公开的启动NettyServer方法，为了以后如果有多种启动方式启动NettyServer时，耦合度低
     * @param hostname
     * @param port
     */
    public static void startServer(String hostname, int port) {
        startServer0(hostname, port);
    }

    /**
     * 初始化NettyServer和启动NettyServer
     * @param hostname
     * @param port
     */
    private static void startServer0(String hostname, int port) {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new NettyMessageEncoder());
                            pipeline.addLast(new NettyMessageDecoder());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture sync = serverBootstrap.bind(hostname, port).sync();
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
