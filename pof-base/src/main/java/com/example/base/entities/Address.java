package com.example.base.entities;

import java.util.Comparator;

public interface Address extends Comparable<Address>{
    /**
     * Get either the public key hash or script hash that is encoded in the address.
     *
     * @return hash that is encoded in the address
     */
    byte[] getHash();
    /**
     * Get the type of output script that will be used for sending to the address.
     *
     * @return type of output script
     */
    ScriptType getOutputScriptType();
}
