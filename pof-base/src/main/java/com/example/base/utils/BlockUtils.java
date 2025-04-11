package com.example.base.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.example.base.entities.Payload;
import com.example.base.entities.block.Block;
import com.example.base.entities.block.BlockHeader;
import com.example.base.entities.transaction.TXInput;
import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class BlockUtils {

    private static final Kryo kryo = new Kryo();

    static {
        kryo.register(Block.class);
        kryo.register(Transaction.class);
        kryo.register(BlockHeader.class);
        kryo.register(Payload.class);
        kryo.register(TXInput.class);
        kryo.register(TXOutput.class);
    }

    public static int getBlockSize(Block block) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeObject(output, block);
        output.close();
        return byteArrayOutputStream.toByteArray().length;
    }

    public static int getTransactionSize(Transaction transaction) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeObject(output, transaction);
        output.close();
        return byteArrayOutputStream.toByteArray().length;
    }

    public static int getPayloadSize(List<Payload> payloadList) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeObject(output, payloadList);
        output.close();
        return byteArrayOutputStream.toByteArray().length;
    }

}
