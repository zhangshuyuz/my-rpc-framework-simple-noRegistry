package com.yuu.rpc.simple.netty.client;

import com.yuu.rpc.simple.netty.codec.NettyMessageDecoder;
import com.yuu.rpc.simple.netty.codec.NettyMessageEncoder;
import com.yuu.rpc.simple.netty.constants.RpcConstants;
import com.yuu.rpc.simple.netty.dto.RpcMessage;
import com.yuu.rpc.simple.netty.dto.RpcRequest;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyClient {

    // 创建一个线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // 创建Handler
    private static NettyClientHandler nettyClientHandler;

    // 创建代理对象
    public Object getBean(final Class<?> serviceClass) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] {serviceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (nettyClientHandler == null) {
                    // 说明是第一次连接
                    initClient();
                }

                // 设置要发送给服务器端的信息
                RpcMessage requestMessage = new RpcMessage();
                RpcRequest request = new RpcRequest();

                request.setInterfaceName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());

                request.setParameters(args);
                request.setParamTypes(method.getParameterTypes());

                requestMessage.setMessageType(RpcConstants.REQUEST_TYPE);
                requestMessage.setData(request);

                nettyClientHandler.setRpcRequest(requestMessage);

                // 使用一个线程，执行客户端发送数据给服务器，并且服务器回送数据给客户端的任务
                // 任务执行成功后，将已经获取到了回送数据的对象作为代理对象返回
                Object o = executor.submit(nettyClientHandler).get();
                RpcMessage result = (RpcMessage)o;
                RpcResponse response =  (RpcResponse)result.getData();
                return response.getData();
            }
        });
    }

    // 初始化客户端
    private static void initClient() {

        nettyClientHandler = new NettyClientHandler();


        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new NettyMessageDecoder());
                        pipeline.addLast(new NettyMessageEncoder());
                        pipeline.addLast(nettyClientHandler);
                    }
                });

        try {
            bootstrap.connect("127.0.0.1", 7000).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
