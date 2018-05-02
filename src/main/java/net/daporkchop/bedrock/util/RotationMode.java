package net.daporkchop.bedrock.util;

import lombok.NonNull;

/**
 * @author DaPorkchop_
 */
public enum RotationMode {
    NORTH((in, out, size, mask, shift, r) -> {
        System.arraycopy(in, 0, out, 0, in.length);
    }, 1),
    EAST((in, out, size, mask, shift, r) -> {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                out[(z << shift) | (x ^ mask)] = in[(x << shift) | z];
            }
        }
    }, 1),
    SOUTH((in, out, size, mask, shift, r) -> {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                out[((x ^ mask) << shift) | (z ^ mask)] = in[(x << shift) | z];
            }
        }
    }, 1),
    WEST((in, out, size, mask, shift, r) -> {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                out[((z ^ mask) << shift) | x] = in[(x << shift) | z];
            }
        }
    }, 1),
    ANY((in, out, size, mask, shift, r) -> {
        switch (r) {
            case 0:
                NORTH.rotator.rotate(in, out, size, mask, shift, r);
                break;
            case 1:
                EAST.rotator.rotate(in, out, size, mask, shift, r);
                break;
            case 2:
                SOUTH.rotator.rotate(in, out, size, mask, shift, r);
                break;
            case 3:
                WEST.rotator.rotate(in, out, size, mask, shift, r);
                break;
            default:
                throw new IllegalStateException();
        }
    }, 4);

    public final int rounds;
    public final Rotator rotator;

    RotationMode(Rotator rotator, int rounds) {
        this.rotator = rotator;
        this.rounds = rounds;
    }

    public interface Rotator {
        /**
         * Rotates an input array and writes to an output array
         *
         * @param in    the input array
         * @param out   the output array
         * @param size  the size (N by N) of the tile to rotate
         * @param mask  the bitmask of the size
         * @param shift the number of bits to shift the X coordinate by
         * @param r     the current round number
         */
        void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r);
    }
}
