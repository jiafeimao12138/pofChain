package com.example.base.entities;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
public class Reward {

    private int low;
    private int medium;
    private int high;
    private int critical;
    private int newPath;

    public Reward() {

    }


    public Reward(int low, int medium, int high, int critical, int newPath) {
        this.low = low;
        this.medium = medium;
        this.high = high;
        this.critical = critical;
        this.newPath = newPath;
    }


}
