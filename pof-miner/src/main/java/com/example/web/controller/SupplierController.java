package com.example.web.controller;

import com.example.base.entities.Message;
import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.fuzzed.ProgramService;
import com.example.base.entities.Reward;
import com.example.web.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("supplier")
public class SupplierController {
    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final MessageService messageService;
    private final ProgramService programService;
    private final Node node;
    private final Reward reward;

    @RequestMapping("/publishMsg")
    public void publishMsg(){
        messageService.publishMsg(new Message("from","to","hello"));
    }

    @RequestMapping("/publishFile")
    public void publishFile(@RequestParam String name, @RequestParam String desc) {
        node.setType(NodeType.SUPPLIER);
        logger.info("node:{}", node);
        // @TODO：supplier上传源代码
//        programService.prepareTargetProgram("supplierfiles/string_length.c",
//                "supplierfiles/string_length_publish", name, desc);
        programService.uploadTask("supplierfiles/potrace.b64", "../programQueue/potrace/src/potrace", name, desc);
    }

    /**
     * supplier设置漏洞奖励和新路径奖励
     */
    @RequestMapping("setReward")
    public void setReward(@RequestParam int lowReward,
                          @RequestParam int mediumReward,
                          @RequestParam int highReward,
                          @RequestParam int criticalReward,
                          @RequestParam int newPathReward
                          ) {
        reward.setRewardValue(Reward.RewardType.LOW, lowReward);
        reward.setRewardValue(Reward.RewardType.MEDIUM, mediumReward);
        reward.setRewardValue(Reward.RewardType.HIGH, highReward);
        reward.setRewardValue(Reward.RewardType.CRITICAL, criticalReward);
        reward.setRewardValue(Reward.RewardType.NEWPATH, newPathReward);
    }

    @RequestMapping("testvue")
    public List<Integer> getIcons() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 100; i >= 0; i--) {
            arrayList.add(i);
        }
        return arrayList;
    }

}
