package com.example.net.events;

import com.example.base.entities.transaction.Transaction;
import org.springframework.context.ApplicationEvent;

/**
 * 新交易事件
 */
public class NewTransactionEvent extends ApplicationEvent {

    public NewTransactionEvent(Transaction transaction) {
        super(transaction);
    }
}
