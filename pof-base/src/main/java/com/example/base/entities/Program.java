package com.example.base.entities;

import lombok.Data;

@Data
public class Program {
    String hash;
    byte[] programCode;
    Peer peer;
    Reward reward;

    public Program(String hash, byte[] programCode, Peer peer, Reward reward) {
        this.hash = hash;
        this.programCode = programCode;
        this.peer = peer;
        this.reward = reward;
    }

    public static void downloadProgram() {

    }

    public static void deleteProgram() {

    }


}
