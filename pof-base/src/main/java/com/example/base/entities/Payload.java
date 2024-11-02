package com.example.base.entities;

import lombok.Data;

@Data
public class Payload {
    private String input;
    private String path;
    private boolean isCrash;

    public Payload(String input, String path, boolean isCrash) {
        this.input = input;
        this.path = path;
        this.isCrash = isCrash;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "input='" + input + '\'' +
                ", path='" + path + '\'' +
                ", isCrash=" + isCrash +
                '}';
    }
}
