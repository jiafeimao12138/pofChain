package com.example.fuzzed;

import com.example.base.entities.NewPath;
import com.example.base.entities.Payload;

import java.util.List;

public interface NewPathService {
    // 对接收到的payload作预处理
    List<NewPath> ProcessPayloads(List<Payload> payloads, long timestamp, String fuzzerAddress);
    // supplier产生本轮挖矿的新路径贡献度排名
    void NewPathContributionRank(List<NewPath> markedNewPaths);
}
