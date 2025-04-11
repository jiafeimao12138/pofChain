package com.example.base.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

// 新路径类
@Data
@Component
@AllArgsConstructor
public class NewPath {
    // 被标记的新路径
//    private List<Integer> path;
    private String pathHash;
    // 探索到该新路径的Fuzzer的地址
    private String FuzzerAddress;
    // 该新路径传过来的时间
    private long timestamp;

    public NewPath() {
    }


}
