package com.yuu.rpc.simple.netty.server;

import com.yuu.rpc.simple.bean.HelloServiceImpl;
import com.yuu.rpc.simple.provider.ServiceProvider;
import com.yuu.rpc.simple.provider.impl.ServiceProviderImpl;
import com.yuu.rpc.simple.publicinterface.HelloService;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动一个服务提供者
 */
public class ServerBootstrap {



    public static void main(String[] args) {

        // 开启服务器端
        NettyServer.startServer("127.0.0.1", 7000);

    }
}
