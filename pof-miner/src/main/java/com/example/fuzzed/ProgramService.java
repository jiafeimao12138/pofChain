package com.example.fuzzed;

import com.example.base.entities.Peer;
import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.Getter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.ArrayDeque;

public interface ProgramService {

    boolean prepareTargetProgram(String path, String objPath);
    String byteToFile(byte[] fileBytes, String path, String name);
    Pair<String, Peer> chooseTargetProgram(String dirPath);
    ArrayDeque<MutablePair<byte[], Peer>> getProgramQueue();
    boolean addProgramQueue(MutablePair<byte[], Peer> pair);
    void setProgramQueue(ArrayDeque<MutablePair<byte[], Peer>> programQueue);
}
