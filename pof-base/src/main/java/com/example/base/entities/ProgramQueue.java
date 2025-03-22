package com.example.base.entities;

import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ProgramQueue {
    private CopyOnWriteArrayList<Program> programList = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String, Program> taskMap = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<Program> getProgramList() {
        return programList;
    }

    public ConcurrentHashMap<String, Program> getTaskMap() {
        return taskMap;
    }

    public boolean updateProgramList(String programHash, long newPathNum, long totalPathNum) {
        Program program = this.taskMap.get(programHash);
        if (program == null)
            return false;
        program.setNewPathNum(program.getNewPathNum() + newPathNum);
        program.setTotalPathNum(program.getTotalPathNum() + totalPathNum);
        return true;
    }

    public boolean addProgramList(Program program) {
        return programList.add(program);
    }

    public boolean addProgramMap(Program program) {
        taskMap.put(program.getHash(), program);
        return true;
    }


    public void setProgramList(CopyOnWriteArrayList<Program> programList) {
        this.programList = programList;
    }


}
