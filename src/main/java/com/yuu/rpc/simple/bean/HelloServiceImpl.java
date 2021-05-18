package com.yuu.rpc.simple.bean;

import com.sun.org.apache.xpath.internal.operations.String;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import com.yuu.rpc.simple.publicinterface.HelloService;

/**
 * 服务提供者
 */
public class HelloServiceImpl implements HelloService {
    /**
     * 当消费方调用该方法时，返回一个结果
     * @param msg
     * @return
     */

    @Override
    public RpcResponse hello(java.lang.String msg) {
        System.out.println("Provider已经收到客户端数据");
        RpcResponse response = new RpcResponse();
        response.setData("已经OK");
        return response;
    }
}
