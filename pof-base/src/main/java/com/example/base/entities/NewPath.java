package com.example.base.entities;

import lombok.Data;

import java.util.List;

// 新路径类
@Data
public class NewPath {
    // 被标记的新路径
    private List<Integer> path;
    // 探索到该新路径的Fuzzer的地址
    private String FuzzerAddress;
    // 该新路径传过来的时间
    private long timestamp;

    public NewPath() {
    }

    public NewPath(List<Integer> path, String fuzzerAddress, long timestamp) {
        this.path = path;
        FuzzerAddress = fuzzerAddress;
        this.timestamp = timestamp;
    }
}
