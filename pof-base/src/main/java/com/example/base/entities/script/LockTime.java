package com.example.base.entities.script;

import com.example.base.utils.TimeUtils;

import java.time.Instant;
import java.util.Objects;

public abstract /* sealed */ class LockTime {

    /**
     * A {@code LockTime} instance that contains a block height.
     * Can also be zero to represent no-lock.
     */
    public static final class HeightLock extends LockTime {
        private HeightLock(long value) {
            super(value);
        }

        /**
         * @return block height as an int
         */
        public int blockHeight() {
            return Math.toIntExact(value);
        }
    }

    /**
     * A {@code LockTime} instance that contains a timestamp.
     */
    public static final class TimeLock extends LockTime {
        private TimeLock(long value) {
            super(value);
        }

        /**
         * @return timestamp in java.time format
         */
        public Instant timestamp() {
            return Instant.ofEpochSecond(value);
        }
    }

    /**
     * Wrap a raw value (as used in the Bitcoin protocol) into a lock time.
     * @param rawValue raw value to be wrapped
     * @return wrapped value
     */
    public static LockTime of(long rawValue) {
        if (rawValue < 0)
            throw new IllegalArgumentException("illegal negative lock time: " + rawValue);
        return rawValue < LockTime.THRESHOLD
                ? new HeightLock(rawValue)
                : new TimeLock(rawValue);
    }

    /**
     * Wrap a block height into a lock time.
     * @param blockHeight block height to be wrapped
     * @return wrapped block height
     */
    public static HeightLock ofBlockHeight(int blockHeight) {
        if (blockHeight < 0)
            throw new IllegalArgumentException("illegal negative block height: " + blockHeight);
        if (blockHeight >= THRESHOLD)
            throw new IllegalArgumentException("block height too high: " + blockHeight);
        return new HeightLock(blockHeight);
    }

    /**
     * Wrap a timestamp into a lock time.
     * @param time timestamp to be wrapped
     * @return wrapped timestamp
     */
    public static TimeLock ofTimestamp(Instant time) {
        long secs = time.getEpochSecond();
        if (secs < THRESHOLD)
            throw new IllegalArgumentException("timestamp too low: " + secs);
        return new TimeLock(secs);
    }

    /**
     * Construct an unset lock time.
     * @return unset lock time
     */
    public static LockTime unset() {
        return LockTime.ofBlockHeight(0);
    }

    /**
     * Raw values below this threshold specify a block height, otherwise a timestamp in seconds since epoch.
     * Consider using {@code lockTime instance of HeightLock} or {@code lockTime instance of TimeLock} before using this constant.
     */
    public static final long THRESHOLD = 500000000; // Tue Nov  5 00:53:20 1985 UTC

    protected final long value;

    private LockTime(long rawValue) {
        this.value = rawValue;
    }

    /**
     * Gets the raw value as used in the Bitcoin protocol
     * @return raw value
     */
    public long rawValue() {
        return value;
    }

    /**
     * The lock time is considered to be set only if its raw value is greater than zero.
     * In other words, it is set if it is either a non-zero block height or a timestamp.
     * @return true if lock time is set
     */
    public boolean isSet() {
        return value > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return this.value == ((LockTime) o).value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return this instanceof HeightLock ?
                "block " + ((HeightLock) this).blockHeight() :
                TimeUtils.dateTimeFormat(((TimeLock) this).timestamp());
    }
}
