package com.example.base.entities;

import com.example.base.entities.block.Block;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Payloads {
    private List<Payload> payloadList = new ArrayList<>();
    private String address;
    private Block newBlock;

    public Payloads() {
    }

    public Payloads(List<Payload> payloadList, Block newBlock, String address) {
        this.payloadList = payloadList;
        this.newBlock = newBlock;
        this.address = address;
    }

    public List<Payload> getPayloads() {
        return payloadList;
    }

    public void setPayloads(List<Payload> payloadList) {
        this.payloadList = payloadList;
    }

    public void addPayloads(List<Payload> payloadList1) {
        this.payloadList.addAll(payloadList1);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setNewBlock(Block newBlock) {
        this.newBlock = newBlock;
    }

    public String getAddress() {
        return address;
    }

    public Block getNewBlock() {
        return newBlock;
    }

    public void setNull() {
        this.payloadList.clear();
    }
}
