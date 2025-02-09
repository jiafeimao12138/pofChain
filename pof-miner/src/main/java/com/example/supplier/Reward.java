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
        NEWPATH,
        NPATH_QUOTA
    }


    private final Map<REWARDTYPE, Integer> RewardHashmap = new HashMap<>();

    private REWARDTYPE type;
    public Reward(REWARDTYPE type) {
        this.type = type;
    }

    public void setRewardValue(REWARDTYPE rewardType, int reward) {
        RewardHashmap.put(rewardType, reward);
    }

    public int getRewardValue(REWARDTYPE rewardType) {
        return RewardHashmap.get(rewardType);
    }
}
