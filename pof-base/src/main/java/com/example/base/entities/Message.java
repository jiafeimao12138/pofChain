package com.example.base.entities;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Message {
    public static final int MSG_VERSION = 1;
    private int version;
    //	从谁，sender
    private String from;
    //	发给谁 receiver
    private String to;
    private BigDecimal value;
    private String text;
    private Long timestamp;
    private String pubKey;

    public Message() {

    }

    public Message(String from, String to, String text){
        this.from = from;
        this.to = to;
        this.version = MSG_VERSION;
        this.timestamp = System.currentTimeMillis();
        this.text = text;
    }
}
