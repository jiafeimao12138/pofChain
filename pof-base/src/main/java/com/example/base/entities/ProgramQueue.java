package com.example.base.entities;

import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;

@Component
public class ProgramQueue {
    private ArrayDeque<Program> ProgramQueue = new ArrayDeque<>(16);

    public ArrayDeque<Program> getProgramQueue() {
        return ProgramQueue;
    }

    public boolean addProgramQueue(Program program) {
        return ProgramQueue.offer(program);
    }

    public void setProgramQueue(ArrayDeque<Program> programQueue) {
        this.ProgramQueue = programQueue;
    }


}
