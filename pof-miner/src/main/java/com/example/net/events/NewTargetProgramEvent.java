package com.example.net.events;

import com.example.base.entities.Peer;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEvent;


public class NewTargetProgramEvent extends ApplicationEvent {
    private Peer peer;
    private byte[] fileBytes;
    private MutablePair<byte[], Peer> pair;
    public NewTargetProgramEvent(MutablePair<byte[], Peer> pair) {
        super(pair);
        this.pair = pair;
        this.peer = pair.getRight();
        this.fileBytes = pair.getLeft();
    }

    public Peer getPeer() {
        return peer;
    }

    public Pair<byte[], Peer> getPair() {
        return pair;
    }


}
