package com.example.supplier;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Reward {
    public enum REWARDTYPE {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
        NEWPATH,           // 每条新路径奖励
        NPATH_QUOTA        // 每次奖励前几名
    }


    private final Map<REWARDTYPE, Integer> RewardHashmap;

    // 初始化
    {
        RewardHashmap = new HashMap<>();
        this.RewardHashmap.put(REWARDTYPE.NEWPATH, 0);
        this.RewardHashmap.put(REWARDTYPE.NPATH_QUOTA, 0);
        this.RewardHashmap.put(REWARDTYPE.CRITICAL, 0);
        this.RewardHashmap.put(REWARDTYPE.HIGH, 0);
        this.RewardHashmap.put(REWARDTYPE.LOW, 0);
        this.RewardHashmap.put(REWARDTYPE.MEDIUM, 0);
    }


    public Reward() {

    }

    public void setRewardValue(REWARDTYPE rewardType, int reward) {
        RewardHashmap.put(rewardType, reward);
    }

    public int getRewardValue(REWARDTYPE rewardType) {
        return RewardHashmap.get(rewardType);
    }
}
