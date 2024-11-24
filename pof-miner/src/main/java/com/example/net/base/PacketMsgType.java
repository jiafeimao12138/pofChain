package com.example.net.base;

public enum PacketMsgType {

    SUCEESS("成功"),
    FAIL("失败"),
    FAIL_NO_HEIGHT_BLOCK("不存在该高度的区块"),
    ;

    private final String description;

    PacketMsgType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
