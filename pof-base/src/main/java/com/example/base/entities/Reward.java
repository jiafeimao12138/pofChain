package com.example.base.entities;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Reward {
    public enum RewardType {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        NEWPATH,           // 每条新路径奖励
    }


    private final Map<RewardType, Integer> RewardHashmap;

    // 初始化
    {
        RewardHashmap = new HashMap<>();
        this.RewardHashmap.put(RewardType.NEWPATH, 0);
        this.RewardHashmap.put(RewardType.CRITICAL, 0);
        this.RewardHashmap.put(RewardType.HIGH, 0);
        this.RewardHashmap.put(RewardType.LOW, 0);
        this.RewardHashmap.put(RewardType.MEDIUM, 0);
    }


    public Reward() {

    }

    public void setRewardValue(RewardType rewardType, int reward) {
        RewardHashmap.put(rewardType, reward);
    }

    public int getRewardValue(RewardType rewardType) {
        return RewardHashmap.get(rewardType);
    }
}
