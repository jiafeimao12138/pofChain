package com.example.net.base;

import lombok.Data;

@Data
public class PacketBody {
    private Object item;
    // operation result
    private boolean success = false;
    // error message
    private String message;

    public PacketBody() {
    }

    public PacketBody(Object item, boolean status) {
        this.item = item;
        this.success = status;
    }
}
