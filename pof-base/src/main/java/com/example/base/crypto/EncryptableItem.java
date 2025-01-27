package com.example.base.crypto;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

public interface EncryptableItem {
    /** Returns whether the item is encrypted or not. If it is, then {@link #getSecretBytes()} will return null. */
    boolean isEncrypted();

    /** Returns the raw bytes of the item, if not encrypted, or null if encrypted or the secret is missing. */
    @Nullable
    byte[] getSecretBytes();

    /** Returns the initialization vector and encrypted secret bytes, or null if not encrypted. */
//    @Nullable
//    EncryptedData getEncryptedData();

    /** Returns an enum constant describing what algorithm was used to encrypt the key or UNENCRYPTED. */
//    Protos.Wallet.EncryptionType getEncryptionType();

    /** Returns the time at which this encryptable item was first created/derived, or empty of unknown. */
    Optional<Instant> getCreationTime();
}
