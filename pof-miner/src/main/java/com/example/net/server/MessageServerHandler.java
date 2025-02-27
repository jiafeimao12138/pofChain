package com.example.net.server;

import com.example.base.entities.*;
import com.example.base.entities.block.Block;
import com.example.base.entities.transaction.Transaction;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import com.example.exception.TransactionNotExistException;
import com.example.fuzzed.NewPathService;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import com.example.net.base.PacketMsgType;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.net.events.NewPeerEvent;
import com.example.net.events.NewTransactionEvent;
import com.example.net.events.TerminateAFLEvent;
import com.example.supplier.Reward;
import com.example.web.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.ClientChannelContext;
import org.tio.core.Node;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// 处理其他node发送的message request
@Component
@RequiredArgsConstructor
public class MessageServerHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);

    private final PeerService peerService;
    private final ValidationService validationService;
    private final ChainService chainService;
    private final P2pClient p2pClient;
    private final NewPathService newPathService;
    private final Payloads payloads;
    private final NewPathManager newPathManager;
    private final ProgramQueue programQueue;
    private final ProcessService processService;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final com.example.base.entities.Node node;
    private final DBStore dbStore;
    private final Reward reward;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();


    public synchronized MessagePacket helloMessage(byte[] msgBody) {
        Message message = (Message) SerializeUtils.unSerialize(msgBody);
        PacketBody packetBody = new PacketBody(message, true);
        return null;
    }

    public synchronized void newPeerConnect(byte[] msgBody) throws Exception {
        Peer peer = (Peer) SerializeUtils.unSerialize(msgBody);
        // 如果该节点之前没有连过，将它添加到数据库中
        if (!peerService.hasPeer(peer)) {
            peerService.addPeer(peer);
        }
        // TODO：接收到hello消息的回复才认为连接成功
        if (p2pClient.connect(new Node(peer.getIp(),peer.getPort())) != null) {
            logger.info("已连接新节点：{}", peer);
            ApplicationContextProvider.publishEvent(new NewPeerEvent(peer));
        }
    }

    //fuzzer处理接收到的新区块
    public synchronized MessagePacket receiveNewBlock_fuzzer(byte[] msgBody) throws TransactionNotExistException {
        Block newBlock = (Block) SerializeUtils.unSerialize(msgBody);
        // 先检查该区块是否本地已存在
        if (chainService.getBlockByHash(newBlock.getHash()) != null) {
            logger.info("该高度已有区块");
            return null;
        }
        if (!validationService.processNewMinedBlock(newBlock)) {
            logger.info("校验新区块失败, hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
            return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, false), "校验新区块失败");
        }
        // 移除已打包的交易
        validationService.removeTransactions(newBlock);
        logger.info("校验新区块成功并存入数据库，hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
        // 广播给其他peer
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
        // 存储新区块后，需要汇报该Fuzzer自己本轮挖掘出的path信息,并附上新区块
        payloads.setNewBlock(newBlock);
        // fuzzerAddress获取
        String address = walletService.getWalletAddress(0);
        payloads.setAddress(address);
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.PAYLOADS_SUBMIT);
        messagePacket.setBody(SerializeUtils.serialize(payloads));
        // 发送给supplier
        Peer supplier = peerService.getSupplierPeer();
        List<ClientChannelContext> channelContextList = p2pClient.getChannelContextList();
        // 在维护的列表中查找supplier
        for (ClientChannelContext channelContext : channelContextList) {
            Node serverNode = channelContext.getServerNode();
            if (serverNode.getIp().equals(supplier.getIp()) && serverNode.getPort() == supplier.getPort()) {
                // supplier在列表中，直接发送消息即可
                p2pClient.sendToNode(channelContext, messagePacket);
                payloads.setNull();
            }
        }
        // 如果没查到，重新连接
        try {
            ClientChannelContext channelContext = p2pClient.connect(new Node(supplier.getIp(), supplier.getPort()));
            p2pClient.sendToNode(channelContext, messagePacket);
            payloads.setNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, true), "成功");
    }

    // observer处理接收到的新节点，不需要提交payloads
    public synchronized MessagePacket receiveNewBlock_observer(byte[] msgBody) throws TransactionNotExistException {
        Block newBlock = (Block) SerializeUtils.unSerialize(msgBody);
        // 先检查该区块是否本地已存在
        if (chainService.getBlockByHash(newBlock.getHash()) != null) {
            logger.info("该高度已有区块");
            return null;
        }
        if (!validationService.processNewMinedBlock(newBlock)) {
            logger.info("校验新区块失败, hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
            return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, false), "校验新区块失败");
        }
        // 移除已打包的交易
        validationService.removeTransactions(newBlock);
        logger.info("校验新区块成功并存入数据库，hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
        // 广播给其他peer
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
        return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, true), "成功");
    }

    // supplier处理接收到的新节点, 如果成功添加新区块，则发布新路径贡献排名
    public synchronized MessagePacket receiveNewBlock_supplier(byte[] msgBody) throws Exception {
        Block newBlock = (Block) SerializeUtils.unSerialize(msgBody);
        logger.info("supplier接收到的newblock: {}", newBlock.toString());
        // 先检查该区块是否本地已存在
        if (chainService.getBlockByHash(newBlock.getHash()) != null) {
            logger.info("该高度已有区块");
            return null;
        }
        if (!validationService.processNewMinedBlock(newBlock)) {
            logger.info("校验新区块失败, hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
            return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, false), "校验新区块失败");
        }
        // 移除已打包的交易
        validationService.removeTransactions(newBlock);
        logger.info("校验新区块成功并存入数据库，hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
        // 广播给其他peer
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
        // 发布新路径贡献度排名
        HashMap<String, List<NewPath>> pathMap = newPathManager.getPaths();
        Map<String, List<NewPath>> newPathContributionRank = newPathService.NewPathContributionRank(pathMap);
        // 发送新路径奖励
        // TODO: 发送新路径奖励要这样强制么？目前交易费设置为0
        ArrayList<Map.Entry<String, List<NewPath>>> rankList = new ArrayList<>(newPathContributionRank.entrySet());
        int newPathQuota = reward.getRewardValue(Reward.REWARDTYPE.NPATH_QUOTA);
        for (int i = 0; i < Math.min(newPathQuota, rankList.size()); i++) {
            Map.Entry<String, List<NewPath>> entry = rankList.get(i);
            List<NewPath> newPathList = entry.getValue();
            String address = entry.getKey();
            int rewardValue = newPathList.size() * Transaction.NEW_PATH_REWARD;
            Transaction rewardTX = transactionService.createFuzzingRewardTransaction(walletService.getWalletAddress(0), address, rewardValue);
            // 广播
            ApplicationContextProvider.publishEvent(new NewTransactionEvent(rewardTX));
        }
        logger.info("supplier已将本轮新路径奖励全部发放");
        return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, true), "成功");
    }

    //处理接收到的新消息
    public synchronized MessagePacket receiveNewMsg(byte[] msgBody) {
        Message message = (Message) SerializeUtils.unSerialize(msgBody);
        logger.info("接收到一个新消息，{}", message.getText());
        return buildPacket(MessagePacketType.RES_NEW_MESSAGE, new PacketBody(message, true), "成功");
    }

    public synchronized MessagePacket receiveGetBlocksReq(byte[] msgBody) {
        Block block = (Block) SerializeUtils.unSerialize(msgBody);
        if( chainService.getBlockByHash(block.getHash()) != null ){

        }
        return buildPacket(MessagePacketType.RES_BLOCKS, new PacketBody(), "不存在该区块");
    }

    public synchronized MessagePacket receiveGetBlockByHeight(byte[] msgBody) {
        long height = (long) SerializeUtils.unSerialize(msgBody);
        Block block = chainService.getBlockByHeight(height);
        if (block == null) {
            // 请求的高度没有区块
            return buildPacket(MessagePacketType.RES_BLOCK_BY_HEIGHT, new PacketBody(block, PacketMsgType.FAIL_NO_HEIGHT_BLOCK), "不存在该区块");
        }
        return buildPacket(MessagePacketType.RES_BLOCK_BY_HEIGHT, new PacketBody(block, PacketMsgType.SUCEESS), "成功");
    }

    public synchronized MessagePacket receiveGetBlockHeaderByHeight(byte[] msgBody) {
        long height = (long) SerializeUtils.unSerialize(msgBody);
        Block block = chainService.getBlockByHeight(height);
        if (block == null) {
            // 请求的高度没有区块
            return buildPacket(MessagePacketType.RES_BLOCK_HEADER, new PacketBody(block, PacketMsgType.FAIL_NO_HEIGHT_BLOCK), "不存在该区块");
        }
        return buildPacket(MessagePacketType.RES_BLOCK_HEADER, new PacketBody(block, PacketMsgType.SUCEESS), "成功");
    }

    public synchronized MessagePacket receiveHeightReq(byte[] msgBody) {
        long height = chainService.getLocalLatestBlock().getBlockHeader().getHeight();
        return buildPacket(MessagePacketType.RES_HEIGHT, new PacketBody(height, PacketMsgType.SUCEESS), "成功");
    }

    public synchronized void receiveFile(MutablePair<byte[], Peer> nodePair, String path, String name) {
        Peer node = nodePair.getRight();
        logger.info("收到file，长度为{}; 发送方：{}:{}", nodePair.getLeft().length, node.getIp(), node.getPort());
        // 将node存入数据库
        if(peerService.addSupplierPeer(nodePair.getRight())){
            logger.info("supplier信息存入数据库");
        }
        // 收到program后，放入队列
        if(programQueue.addProgramQueue(nodePair)){
            ArrayDeque<MutablePair<byte[], Peer>> queue = programQueue.getProgramQueue();
            for (MutablePair<byte[], Peer> peerMutablePair : queue) {
                logger.info("队列中内容：文件长度为{},node为{}",peerMutablePair.getLeft().length,peerMutablePair.getRight());
            }
        }
        logger.info("再获取一次ProgramQueue: {}", programQueue.getProgramQueue());
//        programService.byteToFile(fileByte, path, name);
    }

    public synchronized void receiveNewPathRank(byte[] msgBody) {
        logger.info("收到本轮新路径排名");
        LinkedHashMap<String, List<NewPath>> rank =
                (LinkedHashMap<String, List<NewPath>>) SerializeUtils.unSerialize(msgBody);
        rank.entrySet().stream().forEach(stringListEntry ->
                System.out.println(stringListEntry.getKey() + ":" + stringListEntry.getValue()));
    }

    // supplier接收并处理fuzzer提交的payloads
    public synchronized boolean processPayloads(byte[] msgBody, long timestamp) {
        Payloads payloads = (Payloads) SerializeUtils.unSerialize(msgBody);
        List<Payload> pathList = payloads.getPayloads();
        logger.info("收到的payloads的大小:{}", pathList.size());
        String address = payloads.getAddress();
        Block newBlock = payloads.getNewBlock();
        // 先校验newBlock
        if(validationService.supplierCheckNewBlock(newBlock)) {
            logger.info("通过supplier校验，开始筛选NewPath");
            List<NewPath> newPaths = newPathService.ProcessPayloads(pathList, timestamp, address);
            if (newPaths.isEmpty()) {
                logger.info("No new path found");
            }
            // 添加到NewPathMap中，等待排名
            newPathManager.addPathHashMap(address, newPaths);
            HashMap<String, List<NewPath>> newPathMap = newPathManager.getPaths();
            logger.info("NewPathMap: size={}", newPathMap.size());
            for (Map.Entry<String, List<NewPath>> entry : newPathMap.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            return true;
        }
        return false;
    }

    // 处理其他节点的ProgramQueue请求
    public synchronized MessagePacket responseProgramQueue() {
        ArrayDeque<MutablePair<byte[], Peer>> queue = programQueue.getProgramQueue();
        PacketBody packetBody = new PacketBody();
        packetBody.setItem(queue);
        packetBody.setSuccess(true);
        MessagePacket messagePacket = buildPacket(MessagePacketType.PROGRAM_QUEUQ_RESP, packetBody, "成功");
        return messagePacket;
    }

    // 处理终止AFL
    public synchronized void terminatingAFL() {
        node.setType(NodeType.OBSERVER);
        try {
            List<String> processIds = processService.findProcessIds("afl-fuzz");
            logger.info("suspendFuzzing processIds: {}", processIds);
            if (processIds.size() > 1) {
                logger.error("fuzzing进程数大于1");
            } else if (processIds.size() == 0) {
                logger.info("无AFL进程");
            }
            else {
                String processID = processIds.get(0);
                processService.stopProcess(processID);
                logger.info("已终止Fuzzing进程，进程号{}", processID);
                ApplicationContextProvider.publishEvent(new TerminateAFLEvent(1));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理收到的交易
     * @param transaction
     * @return 返回true表示该交易合法
     */
    public synchronized boolean processNewTransaction(Transaction transaction) {
        try {
            if (!transactionService.verify(transaction)) {
                return false;
            }
            // 存储到mempool
            transactionService.storeMempool(transaction);
            // 持久化存储
            writeLock.lock();
            String txId = ByteUtils.bytesToHex(transaction.getTxId());
            dbStore.put(WalletPrefix.TX_PREFIX.getPrefix() + txId, transaction);
            dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, transaction.getOutputs());
            //@TODO 后面看要不要手动释放
//        dbStore.close();
            writeLock.unlock();
            logger.info("已将交易{}存入数据库", txId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MessagePacket buildPacket(byte type, PacketBody packetBody, String message)
    {
        MessagePacket resPacket = new MessagePacket();
        packetBody.setMessage(message);
        resPacket.setType(type);
        resPacket.setBody(SerializeUtils.serialize(packetBody));
        return resPacket;
    }
}
