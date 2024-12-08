package com.example.base.entities;

// 节点类型
public enum NodeType {
    SUPPLIER("A node which publishes programs to be fuzzed"),
    FUZZER("A node which execute fuzzing and explore new paths"),
    OBSERVER("A standard node which just observes the chain")
    ;

    private final String description;
    NodeType(String description) {
        this.description = description;
    }
}
