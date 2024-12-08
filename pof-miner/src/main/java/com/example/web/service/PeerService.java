package com.example.web.service;

import com.example.base.entities.Peer;

public interface PeerService {
    String PEER_PREFIX = "/peer/";
    String SUPPLIER_PREFIX = "/supplier/";

    // 判断是否存在该peer
    boolean hasPeer(Peer peer);
    // 添加普通peer
    boolean addPeer(Peer peer);
    // 添加供应商peer
    boolean addSupplierPeer(Peer peer);
    // 获取当前supplier
    Peer getSupplierPeer();
    // 该程序fuzzing完毕后，移除supplier
    boolean removeSupplier(Peer peer);
}
