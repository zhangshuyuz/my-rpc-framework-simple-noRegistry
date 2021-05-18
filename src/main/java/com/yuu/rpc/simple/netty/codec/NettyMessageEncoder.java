package com.yuu.rpc.simple.netty.codec;

import com.yuu.rpc.simple.netty.constants.RpcConstants;
import com.yuu.rpc.simple.netty.dto.RpcMessage;
import com.yuu.rpc.simple.serialize.Serializer;
import com.yuu.rpc.simple.serialize.protostuff.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Netty编码器
 * 自定义的协议如下
 * 0     1     2     3     4        5     6     7     8         9          10      11     12  13  14
 *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+
 *  *   |   magic   code        |version | full length         | messageType|    RequestId       |
 *  *   +-----------------------+--------+---------------------+-----------+-----------+----------/
 *  *   |                                                                                         /
 *  *   |                                         body                                            /
 *  *   |                                                                                         /
 *  *   |                                        ... ...                                          /
 *  *   +-----------------------------------------------------------------------------------------/
 */
@Slf4j
public class NettyMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    // 使用原子整型，保证每次的RequestId都是线程安全的
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {

        try {
            /************ 对要发送的数据，按照自定义的协议进行编码  **************/
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstants.VERSION);
            // 因为必须协议中需要有fulllength的位置，因此让writeIndex前进4个字节，给fulllength留个位置
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);

            // 获取发送数据的MessageType
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement()); // 写入RequestId
            // 序列化消息体，并且获得整个消息的长度
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            // 序列化消息体
            Serializer serializer = new ProtostuffSerializer();
            bodyBytes = serializer.serialize(rpcMessage.getData());

            // fullLength = head length + body length
            fullLength += bodyBytes.length;

            byteBuf.writeBytes(bodyBytes);

            // 回到给fullLength预留的位置，写入fullLength
            int writeIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            byteBuf.writeInt(fullLength);
            // 写入fullLength完毕，重写让WriteIndex回到它原有的位置
            byteBuf.writerIndex(writeIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
