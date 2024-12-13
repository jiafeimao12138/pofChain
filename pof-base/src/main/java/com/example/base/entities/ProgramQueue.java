package com.example.base.entities;

import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;

@Component
public class ProgramQueue {
    private ArrayDeque<MutablePair<byte[], Peer>> ProgramQueue = new ArrayDeque<>(16);

    public ArrayDeque<MutablePair<byte[], Peer>> getProgramQueue() {
        return ProgramQueue;
    }

    public boolean addProgramQueue(MutablePair<byte[], Peer> pair) {
        return ProgramQueue.offer(pair);
    }

    public void setProgramQueue(ArrayDeque<MutablePair<byte[], Peer>> programQueue) {
        this.ProgramQueue = programQueue;
    }
}
