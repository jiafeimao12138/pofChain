package com.example.base.entities;

import com.example.base.entities.block.Block;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@AllArgsConstructor
@Data
public class PayloadManager {
    private String programHash;
//    private CopyOnWriteArrayList<Payload> payloadList = new CopyOnWriteArrayList<>();
    private String address;
    private Block newBlock;
    private String signature;

    // 新改的
    private ArrayList<String> pathHashList = new ArrayList<>();
    private HashMap<String, String> crashMap = new HashMap<>();
    // 新改的

    public PayloadManager() {
    }


    // 新改的
    public void addPathHashList(List<String> hashList) {
        this.pathHashList.addAll(hashList);
    }

    public void addCrashMap(HashMap<String, String> crashMap) {
        this.crashMap.putAll(crashMap);
    }
    // 新改的



//    public void addPayloads(CopyOnWriteArrayList<Payload> payloadList1) {
//        this.payloadList.addAll(payloadList1);
//    }

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
//        this.payloadList.clear();
        this.pathHashList.clear();
        this.crashMap.clear();
    }
}
