package com.example.fuzzed;

import com.example.base.entities.Peer;
import com.example.base.entities.Program;
import com.example.base.entities.Reward;
import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.Getter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public interface ProgramService {

    boolean uploadTask(String dirPath, String fuzzPath, String name, String desc, Reward reward);
//    boolean prepareTargetProgram(String path, String objPath, String name, String desc);
    String byteToFile(byte[] fileBytes, String path, String name);
    Pair<String, Peer> chooseTargetProgram(String dirPath, CopyOnWriteArrayList<Program> queue);
    boolean receiveTask(Program program, String directorypath) throws IOException;
    boolean receiveProgram(Program program, String path);
    ConcurrentHashMap<String, Program> getTasks();
    int getCrashNum();
}
