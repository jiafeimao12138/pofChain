package com.example.web.service;

import com.example.base.entities.Peer;
import org.tio.client.ClientChannelContext;

public interface PeerService {
    String PEER_PREFIX = "/peer/";
    String SUPPLIER_PREFIX = "/supplier/";
    // 判断是否存在该peer
    boolean hasPeer(Peer peer);
    // 添加普通peer
    boolean addPeer(Peer peer);
    // 添加供应商peer
    boolean addSupplierPeer(Peer peer, ClientChannelContext channelContext);
    // 该程序fuzzing完毕后，移除supplier
    boolean removeSupplier(Peer peer);
}
