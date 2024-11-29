package com.example.miner;

import com.example.base.entities.NewPath;
import com.example.fuzzed.impl.NewPathServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;

public class TestNewPathList {

    NewPathServiceImpl newPathService;

    @Test
    public void testcontributorRank() {
        ArrayList<NewPath> newPaths = Lists.newArrayList();
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "1", 1734295234));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "2", 1734295236));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1", 1734295299));
        newPathService.NewPathContributionRank(newPaths);
    }
}
