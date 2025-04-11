package com.example.base.entities;

import com.example.base.utils.LoggingMonitor;
import lombok.Data;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Component
@Data
public class NewPathManager {
    private HashMap<String, List<NewPath>> paths = new HashMap<>();
    private HashSet<String> crashSet = new HashSet<>();
    private HashMap<String, HashSet<String>> programCrashInfo = new HashMap<>();
    // 该supplier所有的program的路径结果
    private HashMap<String, MutablePair<Long, Long>> programPathInfo = new HashMap<>();
    private long totalPathNum;
    private long newPathNum;
    private int crashNum;

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
        // 记录日志
        LoggingMonitor.logChange(programHash.substring(0,4), pair);
    }

    public void updateProgramCrashInfo(String programHash, String crash) {
        if(addCrashSet(crash)) {
            programCrashInfo.put(programHash, this.crashSet);
            // 记录日志
            LoggingMonitor.logCrash(programHash, this.crashSet.size());
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

    public boolean addCrashSet(String crash) {
        return this.crashSet.add(crash);
    }
    public int getCrashNum() {
        return this.crashSet.size();
    }

}
