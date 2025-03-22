package com.example.base.entities;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class NewPathManager {
    private HashMap<String, List<NewPath>> paths = new HashMap<>();
    private HashMap<String, MutablePair<Long, Long>> programPathInfo = new HashMap<>();
    private long totalPathNum;
    private long newPathNum;

    public boolean addPathHashMap(String address, List<NewPath> paths) {
        if (this.paths.containsKey(address)) {
            return this.paths.get(address).addAll(paths);
        }
        this.paths.put(address, paths);
        return true;
    }

    public void updateProgramPathInfo(String programHash, long newPathNum, long totalPathNum) {
        MutablePair<Long, Long> pair = programPathInfo.get(programHash);
        if (pair == null) {
            programPathInfo.put(programHash, new MutablePair<>(newPathNum, totalPathNum));
        } else {
            pair.setLeft(pair.getLeft() + newPathNum);
            pair.setRight(pair.getRight() + totalPathNum);
            programPathInfo.put(programHash, pair);
        }
    }

    public void clearPathMap() {
        paths.clear();
    }

    public HashMap<String, List<NewPath>> getPaths() {
        return paths;
    }

    public long getTotalPath() {
        return this.totalPathNum;
    }

    public void setTotalPath(long num) {
        this.totalPathNum = num;
    }

}
