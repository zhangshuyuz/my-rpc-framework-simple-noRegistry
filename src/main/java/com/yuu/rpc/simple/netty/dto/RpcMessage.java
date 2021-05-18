package com.yuu.rpc.simple.netty.dto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    //rpc message type
    private byte messageType;
    //serialization type
    private byte codec;
    //request id
    private int requestId;
    //request data
    private Object data;

}
