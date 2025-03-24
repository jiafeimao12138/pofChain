package com.example.net.listener;

import com.example.base.entities.transaction.Transaction;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.NewTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockEventListener.class);
    private final P2pClient p2pClient;

    public TransactionListener(P2pClient p2pClient) {
        this.p2pClient = p2pClient;
    }

    /**
     * 监听新交易事件
     * @param event
     */
    @EventListener(NewTransactionEvent.class)
    public void onNewTransaction(NewTransactionEvent event) {
        Transaction transaction = (Transaction) event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.BROADCAST_TX);
        messagePacket.setBody(SerializeUtils.serialize(transaction));
        p2pClient.sendToGroup(messagePacket);
    }
}
