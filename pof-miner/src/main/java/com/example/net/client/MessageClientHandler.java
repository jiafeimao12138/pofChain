package com.example.net.client;

import com.example.base.entities.Block;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.PacketBody;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlockByHeightEvent;
import com.example.web.service.ChainService;
import com.example.web.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 处理其他 node 发送的response
@Component
@RequiredArgsConstructor
public class MessageClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageClientHandler.class);
    private final ChainService chainService;
    private final ValidationService validationService;

    // 处理请求某高度区块时接收到的res
    public void receiveGetBlockByHeightRes(byte[] body) {
        PacketBody packetBody = (PacketBody) SerializeUtils.unSerialize(body);
        if (!packetBody.isSuccess()) {
            return;
        }
        //先判断接收到的区块是否已经在本地区块链中存在
        Block block = (Block) packetBody.getItem();
        if (chainService.getBlockByHash(block.getHash()) != null) {
            //说明本地区块链中已经存在该区块了, 那就直接请求下一个区块
            ApplicationContextProvider.publishEvent(new GetBlockByHeightEvent(block.getBlockHeader().getHeight() + 1));
            return;
        }
        // 如果本地区块链中不存在该区块, 则进行合法性校验
        if (validationService.checkBlock(block)) {
            if (validationService.storeBlock(block)) {
                // 已经获取了最新的区块，停止获取
                if (chainService.getMainChainHeight() == block.getBlockHeader().getHeight()){
                    logger.info("已经获取最新区块啦");
                    return;
                }
                ApplicationContextProvider.publishEvent(new GetBlockByHeightEvent(block.getBlockHeader().getHeight() + 1));
                logger.info("请求同步下一个高度的区块，{}", block.getBlockHeader().getHeight() + 1);
            }
            // @TODO：存入失败处理
        } else {
            logger.info("不接收该区块");
        }
    }

    public void receiveHeight(byte[] body) {
        PacketBody packetBody = (PacketBody) SerializeUtils.unSerialize(body);
        if (!packetBody.isSuccess()) {
            return;
        }
        long height = (long) packetBody.getItem();
        if (validationService.storeChainHeight(height)) {
            logger.info("更新主链当前高度, {}", height);
        }
    }

}
