//package com.example.base.entities.wallet;
//
//import com.example.base.entities.script.ScriptType;
//import org.bitcoinj.wallet.DeterministicKeyChain;
//
//import java.security.SecureRandom;
//import java.util.LinkedList;
//import java.util.List;
//
//public class KeyChainGroup {
//    public static class Builder {
//        private final List<DeterministicKeyChain> chains = new LinkedList<>();
//        private int lookaheadSize = -1, lookaheadThreshold = -1;
//
//        private Builder() {
//        }
//
//        /**
//         * <p>Add chain from a random source.</p>
//         * <p>In the case of P2PKH, just a P2PKH chain is created and activated which is then the default chain for fresh
//         * addresses. It can be upgraded to P2WPKH later.</p>
//         * <p>In the case of P2WPKH, both a P2PKH and a P2WPKH chain are created and activated, the latter being the default
//         * chain. This behaviour will likely be changed in future such that only a P2WPKH chain is created and
//         * activated.</p>
//         *
//         * @param outputScriptType type of addresses (aka output scripts) to generate for receiving
//         */
//        public Builder fromRandom(ScriptType outputScriptType) {
//            DeterministicSeed seed = DeterministicSeed.ofRandom(new SecureRandom(),
//                    DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "");
//            fromSeed(seed, outputScriptType);
//            return this;
//        }
//
//        public Builder fromSeed(DeterministicSeed seed, ScriptType outputScriptType) {
//            if (outputScriptType == ScriptType.P2PKH) {
//                DeterministicKeyChain chain = DeterministicKeyChain.builder().seed(seed)
//                        .outputScriptType(ScriptType.P2PKH)
//                        .accountPath(structure.accountPathFor(ScriptType.P2PKH, network)).build();
//                this.chains.clear();
//                this.chains.add(chain);
//            }  else {
//                throw new IllegalArgumentException(outputScriptType.toString());
//            }
//            return this;
//        }
//    }
//}
