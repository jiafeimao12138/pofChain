package com.example.base.entities;

import lombok.Data;

@Data
public class Program {
    String hash;
    String name;
    String desc;
    byte[] programCode;
    String fuzzPath;
    Peer peer;
    Reward reward;
    long newPathNum;
    long TotalPathNum;
    long crashNum;

    public Program() {

    }

    public Program(String hash, String name, String desc,  byte[] programCode, String fuzzPath, Peer peer,
                   Reward reward, long newPathNum, long TotalPathNUm, long crashNum) {
        this.hash = hash;
        this.name = name;
        this.desc = desc;
        this.programCode = programCode;
        this.fuzzPath = fuzzPath;
        this.peer = peer;
        this.reward = reward;
        this.newPathNum = newPathNum;
        this.TotalPathNum = TotalPathNUm;
        this.crashNum = crashNum;
    }

    public static void downloadProgram() {

    }

    public static void deleteProgram() {

    }


}
