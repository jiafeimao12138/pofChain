package com.example.exception;

import com.example.base.entities.transaction.Transaction;

/**
 * 交易不存在异常
 */
public class TransactionNotExistException extends Exception{
    public TransactionNotExistException() {
        super("Transaction not exist");
    }

    public TransactionNotExistException(String str) {
        super(str + "不存在");
    }
}
