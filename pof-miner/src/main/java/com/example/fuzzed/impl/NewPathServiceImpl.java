package com.example.fuzzed.impl;

import com.example.base.entities.NewPath;
import com.example.base.entities.Payload;
import com.example.base.store.DBStore;
import com.example.base.store.PathPrefix;
import com.example.base.utils.CryptoUtils;
import com.example.fuzzed.NewPathService;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewPathRank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewPathServiceImpl implements NewPathService {
    private static final Logger logger = LoggerFactory.getLogger(NewPathServiceImpl.class);

    private final DBStore dbStore;
    @Override
    public List<NewPath> ProcessPayloads(List<Payload> payloads, long timestamp, String fuzzerAddress) {
        HashSet<List<Integer>> paths = new HashSet<>();
        List<NewPath> newPahtList = new ArrayList<>();
        for (Payload payload : payloads) {
            // 如果是潜在漏洞
            if (payload.isCrash()) {
                // @TODO 需要交给supplier手动验证处理
            } else {
                List<Integer> path = payload.getPath();
                paths.add(path);
            }
        }
        //获得去重后的path，对它们进行处理
        for (List<Integer> path : paths) {
            String pathHash = CryptoUtils.SHA256(path.toString());
            Optional<Object> o = dbStore.get(pathHash);
            // 如果hash值存在，则先判断是否hash碰撞，没有碰撞的话表示这个path已经存在，不是新路径，舍弃即可
            if (o.isPresent()) {
                List<Integer> rockspath = (List<Integer>) o.get();
                if (!rockspath.equals(path)) {
                    // @TODO 说明碰撞了

                }
            }else {
                // 数据库中没有该路径，表明这是一个新路径，那么存入数据库中
                dbStore.put(PathPrefix.PATH_PREFIX.getPrefix() + pathHash, path);
                dbStore.put(PathPrefix.PATH_FUZZER_PREFIX.getPrefix() + pathHash, fuzzerAddress );
                dbStore.put(PathPrefix.PATH_TIME_PREFIX.getPrefix() + pathHash, timestamp);
                NewPath newPath = new NewPath();
                newPath.setPath(path);
                newPath.setFuzzerAddress(fuzzerAddress);
                newPath.setTimestamp(timestamp);
                newPahtList.add(newPath);
            }
        }
        return newPahtList;
    }


    @Override
    public void NewPathContributionRank(List<NewPath> markedNewPaths) {
        // 将新路径列表按照Fuzzer分组
        Map<String, List<NewPath>> groupNewPath =
                markedNewPaths.stream().collect(Collectors.groupingBy(NewPath::getFuzzerAddress));
        // 根据Fuzzer挖出的新路径数量倒序排序
        Comparator<List<NewPath>> comparator = new Comparator<List<NewPath>>() {
            @Override
            public int compare(List<NewPath> l1, List<NewPath> l2) {
                return Integer.compare(l2.size(), l1.size());
            }
        };
        // 倒序排序后的map
        LinkedHashMap<String, List<NewPath>> sortedmap = groupNewPath.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1,e2)->e1,
                        LinkedHashMap::new));
        // 广播
        ApplicationContextProvider.publishEvent(new NewPathRank(sortedmap));
        logger.info("已广播本轮Fuzzing的新路径排名");
    }

}
