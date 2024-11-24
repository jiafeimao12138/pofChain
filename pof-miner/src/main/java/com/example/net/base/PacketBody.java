package com.example.net.base;

import lombok.Data;

@Data
public class PacketBody {
    private Object item;
    // operation result
    private boolean isSuccess = false;
    private PacketMsgType packetMsgType;
    // error message
    private String message;

    public PacketBody() {
    }

    public PacketBody(Object item, boolean status) {
        this.item = item;
        this.isSuccess = status;
    }

    public PacketBody(Object item, PacketMsgType packetMsgType) {
        this.item = item;
        this.packetMsgType = packetMsgType;
        this.isSuccess = packetMsgType.equals(PacketMsgType.SUCEESS);
        this.message = packetMsgType.getDescription();
    }
}
