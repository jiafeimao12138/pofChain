package com.example.web.service.impl;

import com.example.base.entities.Peer;
import com.example.base.store.DBStore;
import com.example.web.service.PeerService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class PeerServiceImpl implements PeerService {

    private final DBStore dbStore;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public PeerServiceImpl(DBStore dbStore) {
        this.dbStore = dbStore;
    }

    @Override
    public boolean hasPeer(Peer peer) {
        return dbStore.get(PEER_PREFIX + peer.toString()).isPresent();
    }

    @Override
    public boolean addSupplierPeer(Peer peer) {
        writeLock.lock();
        if (hasPeer(peer) && dbStore.put(SUPPLIER_PREFIX, peer)) {
            writeLock.unlock();
            return true;
        }
        if(dbStore.put(PEER_PREFIX + peer, peer) &
                dbStore.put(SUPPLIER_PREFIX + peer, peer)) {
            writeLock.unlock();
            return true;
        }
        return false;
    }

    @Override
    public Peer getSupplierPeer() {
        readLock.lock();
        Optional<Object> o = dbStore.get(SUPPLIER_PREFIX);
        if (o.isPresent()) {
            Peer node = (Peer) o.get();
            readLock.unlock();
            return node;
        }
        return null;
    }

    @Override
    public boolean removeSupplier(Peer peer) {
        return dbStore.delete(SUPPLIER_PREFIX + peer.toString());
    }

    @Override
    public boolean addPeer(Peer peer) {
        writeLock.lock();
        if(dbStore.put(PEER_PREFIX + peer.toString(), peer)) {
            writeLock.unlock();
            return true;
        }
        return false;
    }
}
