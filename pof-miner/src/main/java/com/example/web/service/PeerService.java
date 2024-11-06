package com.example.web.service;

import com.example.base.entities.Peer;

public interface PeerService {
    String PEER_PREFIX = "/peer/";
    boolean hasPeer(Peer peer);
    boolean addPeer(Peer peer);

}
