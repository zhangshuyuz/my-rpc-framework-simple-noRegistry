package com.yuu.rpc.simple.customer;

import com.yuu.rpc.simple.netty.client.NettyClient;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import com.yuu.rpc.simple.publicinterface.HelloService;

/**
 * 启动一个服务消费者
 */
public class ClientBootstrap {

    public static void main(String[] args) {
        // 创建一个消费者
        NettyClient customer = new NettyClient();
        // 创建一个代理对象
        HelloService helloService = (HelloService)customer.getBean(HelloService.class);

        // 通过代理对象调用服务
        RpcResponse hello = helloService.hello("你好 dubbo");
        System.out.println("服务器端返回的结果为：" + hello.getData());

    }
}
