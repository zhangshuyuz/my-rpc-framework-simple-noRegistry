package com.yuu.rpc.simple.netty.codec;

import com.yuu.rpc.simple.netty.constants.RpcConstants;
import com.yuu.rpc.simple.netty.dto.RpcMessage;
import com.yuu.rpc.simple.netty.dto.RpcRequest;
import com.yuu.rpc.simple.netty.dto.RpcResponse;
import com.yuu.rpc.simple.serialize.Serializer;
import com.yuu.rpc.simple.serialize.protostuff.ProtostuffSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Netty的解码器。
 * 自定义的协议如下图所示
 * 0     1     2     3     4        5     6     7     8   9           10      11       12    13    14
 * +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+------
 * |   magic   code        |version | full length         | messageType|    RequestId              |
 * +-----------------------+--------+---------------------+-----------+-----------+-----------+-----/
 * |                                                                                                /
 * |                                         body                                                   /
 * |                                                                                                /
 * |                                        ... ...                                                 /
 * +------------------------------------------------------------------------------------------------/
 */
@Slf4j
public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {

    public NettyMessageDecoder() {
        super(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        ByteBuf frame = (ByteBuf) decoded;
        Object o = decodeFrame(frame);
        return o;

    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 先比较魔术值和版本号
        checkMagicNum(byteBuf);
        checkVersion(byteBuf);

        int fullLength = byteBuf.readInt();
        byte messageType = byteBuf.readByte();
        int requestId = byteBuf.readInt();

        /****************  将数据封装为RPCMessage对象   *****************/
        RpcMessage message = RpcMessage.builder()
                .messageType(messageType)
                .requestId(requestId).build();

        // 整个消息的长度，减去消息头的长度，就是消息体的长度，即真实数据的长度
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            byteBuf.readBytes(bs);
            // 对数据，进行反序列化
            Serializer serializer = new ProtostuffSerializer();
            // 判断序列化的数据，是属于服务提供方的还是服务消费方的
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                message.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                message.setData(tmpValue);
            }
        }

        // 将解码后的数据返回
        return message;
    }

    private void checkVersion(ByteBuf in) {
        // 判断比较Version
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNum(ByteBuf in) {
        // 读取魔术值，然后比较
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

}
