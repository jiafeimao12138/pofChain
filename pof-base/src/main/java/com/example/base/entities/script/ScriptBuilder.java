//package com.example.base.entities.script;
//
//import javax.annotation.Nullable;
//import java.time.Instant;
//import java.util.*;
//
//import static com.example.base.entities.script.ScriptOpCodes.*;
//import static com.example.base.utils.Preconditions.checkArgument;
//import static com.example.base.utils.Preconditions.checkState;
//
//import com.example.base.crypto.ECKey;
//import com.example.base.entities.Address;
//import com.example.base.entities.LegacyAddress;
//import com.example.base.entities.transaction.TransactionSignature;
//
///*
//* Tools for the construction of commonly used script types.
//* */
//public class ScriptBuilder {
//
//    private final List<ScriptChunk> chunks;
//    /**
//     * If this is set, the script to be built is associated with a creation time. This is currently used in the
//     * context of watching wallets only, where the scriptPubKeys being watched actually represent public keys and
//     * their addresses.
//     */
//    @Nullable
//    private Instant creationTime = null;
//
//    /** Creates a fresh ScriptBuilder with an empty program. */
//    public ScriptBuilder() {
//        chunks = new LinkedList<>();
//    }
//
//
//    /** Creates a fresh ScriptBuilder with the given program as the starting point. */
//    public ScriptBuilder(Script template) {
//        chunks = new ArrayList<>(template.chunks());
//    }
//
//    /**
//     * Associates this script to be built with a given creation time. This is currently used in the context of
//     * watching wallets only, where the scriptPubKeys being watched actually represent public keys and their addresses.
//     *
//     * @param creationTime creation time to associate the script with
//     * @return this builder
//     */
//    public ScriptBuilder creationTime(Instant creationTime) {
//        this.creationTime = Objects.requireNonNull(creationTime);
//        return this;
//    }
//
//    /** Adds the given chunk to the end of the program */
//    public ScriptBuilder addChunk(ScriptChunk chunk) {
//        return addChunk(chunks.size(), chunk);
//    }
//
//    /** Adds the given chunk at the given index in the program */
//    public ScriptBuilder addChunk(int index, ScriptChunk chunk) {
//        chunks.add(index, chunk);
//        return this;
//    }
//
//    /** Adds the given opcode to the end of the program. */
//    public ScriptBuilder op(int opcode) {
//        return op(chunks.size(), opcode);
//    }
//
//    /** Adds the given opcode to the given index in the program */
//    public ScriptBuilder op(int index, int opcode) {
//        checkArgument(opcode > OP_PUSHDATA4);
//        return addChunk(index, new ScriptChunk(opcode, null));
//    }
//
//    /** Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the end of the program. */
//    public ScriptBuilder data(byte[] data) {
//        if (data.length == 0)
//            return smallNum(0);
//        else
//            return data(chunks.size(), data);
//    }
//
//    /** Adds a copy of the given byte array as a data element (i.e. PUSHDATA) at the given index in the program. */
//    public ScriptBuilder data(int index, byte[] data) {
//        // implements BIP62
//        byte[] copy = Arrays.copyOf(data, data.length);
//        int opcode;
//        if (data.length == 0) {
//            opcode = OP_0;
//        } else if (data.length == 1) {
//            byte b = data[0];
//            if (b >= 1 && b <= 16)
//                opcode = Script.encodeToOpN(b);
//            else
//                opcode = 1;
//        } else if (data.length < OP_PUSHDATA1) {
//            opcode = data.length;
//        } else if (data.length < 256) {
//            opcode = OP_PUSHDATA1;
//        } else if (data.length < 65536) {
//            opcode = OP_PUSHDATA2;
//        } else {
//            throw new RuntimeException("Unimplemented");
//        }
//        return addChunk(index, new ScriptChunk(opcode, copy));
//    }
//
//    /**
//     * Adds the given number as a OP_N opcode to the end of the program.
//     * Only handles values 0-16 inclusive.
//     *
//     */
//    public ScriptBuilder smallNum(int num) {
//        return smallNum(chunks.size(), num);
//    }
//
//    /**
//     * Adds the given number as a OP_N opcode to the given index in the program.
//     * Only handles values 0-16 inclusive.
//     *
//     */
//    public ScriptBuilder smallNum(int index, int num) {
//        checkArgument(num >= 0, () ->
//                "cannot encode negative numbers with smallNum");
//        checkArgument(num <= 16, () ->
//                "cannot encode numbers larger than 16 with smallNum");
//        return addChunk(index, new ScriptChunk(Script.encodeToOpN(num), null));
//    }
//
//
//    /** Creates a new immutable Script based on the state of the builder. */
//    public Script build() {
//        if (creationTime != null)
//            return Script.of(chunks, creationTime);
//        else
//            return Script.of(chunks);
//    }
//
//    /** Creates an empty script. */
//    public static Script createEmpty() {
//        return new ScriptBuilder().build();
//    }
//
//    /**
//     * Creates a scriptPubKey that encodes payment to the given address.
//     *
//     * @param to           address to send payment to
//     * @param creationTime creation time of the scriptPubKey
//     * @return scriptPubKey
//     */
//    public static Script createOutputScript(Address to, Instant creationTime) {
//        return new ScriptBuilder().outputScript(to).creationTime(creationTime).build();
//    }
//
//    /**
//     * Creates a scriptPubKey that encodes payment to the given address.
//     *
//     * @param to address to send payment to
//     * @return scriptPubKey
//     */
//    public static Script createOutputScript(Address to) {
//        return new ScriptBuilder().outputScript(to).build();
//    }
//
//    private ScriptBuilder outputScript(Address to) {
//        checkState(chunks.isEmpty());
//        if (to instanceof LegacyAddress) {
//            ScriptType scriptType = to.getOutputScriptType();
//            if (scriptType == ScriptType.P2PKH)
//                p2pkhOutputScript(((LegacyAddress) to).getHash());
////            else if (scriptType == ScriptType.P2SH)
////                p2shOutputScript(((LegacyAddress) to).getHash());
//            else
//                throw new IllegalStateException("Cannot handle " + scriptType);
//        }
////        else if (to instanceof SegwitAddress) {
////            p2whOutputScript((SegwitAddress) to);
////        }
//        else {
//            throw new IllegalStateException("Cannot handle " + to);
//        }
//        return this;
//    }
//
//    /**
//     * Creates a scriptSig that can redeem a P2PKH output.
//     * If given signature is null, incomplete scriptSig will be created with OP_0 instead of signature
//     */
//    public static Script createInputScript(@Nullable TransactionSignature signature, ECKey pubKey) {
//        byte[] pubkeyBytes = pubKey.getPubKey();
//        byte[] sigBytes = signature != null ? signature.encodeToBitcoin() : new byte[]{};
//        return new ScriptBuilder().data(sigBytes).data(pubkeyBytes).build();
//    }
//
//    /**
//     * Creates a scriptPubKey that sends to the given public key hash.
//     */
//    public static Script createP2PKHOutputScript(byte[] hash) {
//        return new ScriptBuilder().p2pkhOutputScript(hash).build();
//    }
//
//    /**
//     * 生成P2PKH交易输出脚本scriptPubKey，用于定义如何可以花费该输出的条件。
//     * 在这个情况下，脚本要求提供与 <pubKeyHash> 匹配的公钥和有效的签名才能花费这笔输出
//     * @param hash
//     * @return
//     */
//    private ScriptBuilder p2pkhOutputScript(byte[] hash) {
//        checkArgument(hash.length == LegacyAddress.LENGTH);
//        checkState(chunks.isEmpty());
//        return op(OP_DUP)
//                .op(OP_HASH160)
//                .data(hash)
//                .op(OP_EQUALVERIFY)
//                .op(OP_CHECKSIG);
//    }
//}
