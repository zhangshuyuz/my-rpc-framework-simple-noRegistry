package com.yuu.rpc.simple.publicinterface;

import com.yuu.rpc.simple.netty.dto.RpcResponse;

/**
 * 服务提供方和服务消费方都需要的接口
 */
public interface HelloService {
    RpcResponse hello(String msg);
}
