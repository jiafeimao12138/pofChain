package com.example.base.entities;

import com.example.base.entities.script.ScriptType;

import java.util.Objects;

public class LegacyAddress implements Address{
    /**
     * An address is a RIPEMD160 hash of a public key, therefore is always 160 bits or 20 bytes.
     */
    public static final int LENGTH = 20;
//    protected final Network network;
    protected final byte[] bytes;
    /** True if P2SH, false if P2PKH. */
    public final boolean p2sh;
    private LegacyAddress(boolean p2sh, byte[] hash160){
        this.bytes = Objects.requireNonNull(hash160);
        if (hash160.length != 20){
            //error
        }

        this.p2sh = p2sh;
    }

    @Override
    public byte[] getHash() {
        return new byte[0];
    }

    @Override
    public ScriptType getOutputScriptType() {
        return null;
    }

    /**
     * Construct a {@link LegacyAddress} that represents the given pubkey hash. The resulting address will be a P2PKH type of
     * address.
     *
     *
     * @param hash160 20-byte pubkey hash
     * @return constructed address
     */
    public static LegacyAddress fromPubKeyHash(byte[] hash160){
        return new LegacyAddress(false, hash160);
    }

    @Override
    public int compareTo(Address address) {
        return 0;
    }
}
