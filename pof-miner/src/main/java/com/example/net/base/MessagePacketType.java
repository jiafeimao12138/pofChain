package com.example.net.base;

public interface MessagePacketType
{
    byte HELLO_MESSAGE = 0;

    // new block message request
    byte REQ_NEW_BLOCK = 1;

    // new block confirm
    byte RES_NEW_BLOCK = -1;

    // new message request
    byte REQ_NEW_MESSAGE = 2;

    // new message confirm
    byte RES_NEW_MESSAGE = -2;

    // block sync request
    byte REQ_BLOCK_SYNC = 3;

    // block sync response
    byte RES_BLOCK_SYNC = -3;

    // 新节点连接请求
    byte REQ_NEW_PEER = 4;

    // new peer connected response
    byte RES_NEW_PEER = -4;
}
