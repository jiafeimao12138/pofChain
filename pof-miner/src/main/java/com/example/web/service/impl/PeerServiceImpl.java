package com.example.web.service.impl;

import com.example.base.entities.Peer;
import com.example.base.store.DBStore;
import com.example.web.service.PeerService;
import org.springframework.stereotype.Service;
import org.tio.client.ClientChannelContext;

@Service
public class PeerServiceImpl implements PeerService {

    private final DBStore dbStore;

    public PeerServiceImpl(DBStore dbStore) {
        this.dbStore = dbStore;
    }

    @Override
    public boolean hasPeer(Peer peer) {
        return dbStore.get(PEER_PREFIX + peer.toString()).isPresent();
    }

    @Override
    public boolean addSupplierPeer(Peer peer) {
        if (hasPeer(peer)) {
            return dbStore.put(SUPPLIER_PREFIX + peer.toString(), peer);
        }
        return dbStore.put(PEER_PREFIX + peer.toString(), peer) &
                dbStore.put(SUPPLIER_PREFIX + peer.toString(), peer);
    }

    @Override
    public Peer getSupplierPeer() {
        return null;
    }

    @Override
    public boolean removeSupplier(Peer peer) {
        return dbStore.delete(SUPPLIER_PREFIX + peer.toString());
    }

    @Override
    public boolean addPeer(Peer peer) {
        return dbStore.put(PEER_PREFIX + peer.toString(), peer);
    }
}
