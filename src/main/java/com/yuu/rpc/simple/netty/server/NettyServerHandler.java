package com.yuu.rpc.simple.netty.server;

import com.yuu.rpc.simple.bean.HelloServiceImpl;
import com.yuu.rpc.simple.netty.constants.RpcConstants;
import com.yuu.rpc.simple.netty.dto.RpcMessage;
import com.yuu.rpc.simple.netty.dto.RpcRequest;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import com.yuu.rpc.simple.provider.ServiceProvider;
import com.yuu.rpc.simple.provider.impl.ServiceProviderImpl;
import com.yuu.rpc.simple.publicinterface.HelloService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static ServiceProvider provider = new ServiceProviderImpl();

    static {
        // 注册服务
        HelloService helloService = new HelloServiceImpl();
        provider.addService(helloService);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 获取客户端发送的数据，并调用服务端的api
        RpcMessage message = (RpcMessage)msg;
        RpcRequest requestData = (RpcRequest)message.getData();

        Object service = provider.getService(requestData.getInterfaceName());
        Method method = service.getClass().getMethod(requestData.getMethodName(), requestData.getParamTypes());
        Object result = method.invoke(service, requestData.getParameters());

        // 将服务器端的数据返回给客户端
        RpcMessage resultMessage = new RpcMessage();
        RpcResponse<Object> success = RpcResponse.success(result, message.getRequestId() + "");
        resultMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
        resultMessage.setData(success);

        ctx.writeAndFlush(resultMessage);


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
