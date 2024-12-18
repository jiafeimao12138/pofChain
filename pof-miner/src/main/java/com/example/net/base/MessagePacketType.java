package com.example.net.base;

public interface MessagePacketType
{
    byte HELLO_MESSAGE = 0;
    // 新区块发布
    byte REQ_NEW_BLOCK = 1;
    // 新区块确认response
    byte RES_NEW_BLOCK = -1;
    // new message request
    byte REQ_NEW_MESSAGE = 2;
    // new message confirm
    byte RES_NEW_MESSAGE = -2;
    // 请求blocks
    byte REQ_BLOCKS = 3;
    // 返回请求的blocks
    byte RES_BLOCKS = -3;
    // 新节点连接请求
    byte REQ_NEW_PEER = 4;
    // new peer connected response
    byte RES_NEW_PEER = -4;
    // 根据高度请求block
    byte REQ_BLOCK_BY_HEIGHT = 5;
    byte RES_BLOCK_BY_HEIGHT = -5;
    // 请求当前主链最大高度
    byte REQ_HEIGHT = 6;
    byte RES_HEIGHT = -6;
    // supplier发布待测程序
    byte PUBLISH_FILE = 7;
    byte RECV_FILE = -7;
    // supplier广播新路径排名
    byte NEW_PATH_RANK = 9;
    // fuzzer向supplier提交payloads
    byte PAYLOADS_SUBMIT = 10;
    // 请求ProgramQueue
    byte PROGRAM_QUEUQ_REQ = 11;
    byte PROGRAM_QUEUQ_RESP = -11;
    // 终止Fuzzing
    byte TERMINATE_FUZZING = 12;
}
