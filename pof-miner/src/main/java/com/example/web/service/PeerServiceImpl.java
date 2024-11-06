package com.example.web.service;

import com.example.base.entities.Peer;
import com.example.base.store.DBStore;
import org.springframework.stereotype.Service;

@Service
public class PeerServiceImpl implements PeerService{

    private final DBStore dbStore;

    public PeerServiceImpl(DBStore dbStore) {
        this.dbStore = dbStore;
    }

    @Override
    public boolean hasPeer(Peer peer) {
        return dbStore.get(PEER_PREFIX + peer.toString()).isPresent();
    }

    @Override
    public boolean addPeer(Peer peer) {
        return dbStore.put(PEER_PREFIX + peer.toString(), peer);
    }
}
