package com.example.base.entities;

import lombok.Data;

import java.util.List;

@Data
public class Crash {
    String programHash;
    String input;
    List<Integer> path;

}
