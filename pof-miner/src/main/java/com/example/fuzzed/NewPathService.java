package com.example.fuzzed;

import com.example.base.entities.NewPath;
import com.example.base.entities.Payload;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NewPathService {
    // fuzzer预处理
    List<NewPath> preProcessPayloads(List<Payload> payloads);
    // 对接收到的payload作预处理
    List<NewPath> ProcessPayloads(String programHash, List<Payload> payloads, long timestamp, String fuzzerAddress);
    // supplier产生本轮挖矿的新路径贡献度排名
    Map<String, List<NewPath>> NewPathContributionRank(HashMap<String, List<NewPath>> groupNewPath);

}
