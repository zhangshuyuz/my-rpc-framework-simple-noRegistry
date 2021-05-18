package com.yuu.rpc.simple.netty.client;

import com.yuu.rpc.simple.netty.dto.RpcMessage;
import com.yuu.rpc.simple.netty.dto.RpcRequest;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Callable;

@Getter
@Setter
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private RpcMessage rpcResponse; // 将来服务端返回回来的结果
    private RpcMessage rpcRequest; // 客户端要调用远程方法时，传入的参数

    /**
     * 被代理对象使用，真正发送数据的方法。发送数据后，等待被唤醒
     * @return
     * @throws Exception
     */
    @Override
    public synchronized Object call() throws Exception {
        context.writeAndFlush(rpcRequest); // 发送给服务器消息
        wait(); // 进入wait状态
        // 此时数据发送回来，并且封装给了result属性，因此直接将result返回即可
        return rpcResponse;
    }

    /**
     * 与服务器连接成功后，立即执行该方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 立即将ctx赋值
        this.context = ctx;
    }

    /**
     * 收到服务器数据后，调用该方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 服务器端数据发送回来了，将数据封装给属性
        rpcResponse = (RpcMessage)msg;
        // 唤醒等待的线程
        /* 因为发送数据时，是代理对象调用call方法来发送数据的，并且在发送数据后会进入waiting状态，等待数据返回后才进行下一个动作。
         * 服务端返回数据，是发给channelRead方法的，因此在获取到数据后，应该唤醒之前等待的线程
         */
        notify();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
