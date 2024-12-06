package com.example.net.base;

import lombok.Data;
import org.tio.core.intf.Packet;

@Data
public class MessagePacket extends Packet {
    public static final int HEADER_LENGTH = 5;
    public static final String HELLO_MESSAGE = "Hello pofChain.";
    private byte type;
    private byte[] body;
    private byte[] node;
    public MessagePacket() {
    }
}
