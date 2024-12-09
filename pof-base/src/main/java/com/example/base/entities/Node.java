package com.example.base.entities;

import com.example.base.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.util.Random;

public class Node {
    // 节点地址
    private String address = CryptoUtils.SHA256(new Random().toString());
    // 节点类型，默认为observer
    private NodeType type = NodeType.OBSERVER;

    public Node() {
    }

    public Node(String address) {
        this.address = address;
    }

    public Node(String address, NodeType type) {
        this.address = address;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Node{" +
                "address='" + address + '\'' +
                ", type=" + type +
                '}';
    }
}
