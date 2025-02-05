package com.example.base.entities.script;

public enum ScriptType {
    P2PKH("pkh", 1),    // pay to pubkey hash (aka pay to address)
    P2PK("pk", 2),      // pay to pubkey
    P2SH("sh", 3),      // pay to script hash
    P2WPKH("wpkh", 4),  // pay to witness pubkey hash
    P2WSH("wsh", 5),    // pay to witness script hash
    P2TR("tr", 6);      // pay to taproot

    private final String scriptIdentifierString;
    public final int id;
    ScriptType(String id, int numericId) {
        this.scriptIdentifierString = id;
        this.id = numericId;
    }
}
