package net.daporkchop.bedrock.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public enum RotationMode {
    NORTH(1) {
        @Override
        public void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r) {
            System.arraycopy(in, 0, out, 0, in.length);
        }
    },
    EAST(1) {
        @Override
        public void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    out[(z << shift) | (x ^ mask)] = in[(x << shift) | z];
                }
            }
        }
    },
    SOUTH(1) {
        @Override
        public void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    out[((x ^ mask) << shift) | (z ^ mask)] = in[(x << shift) | z];
                }
            }
        }
    },
    WEST(1) {
        @Override
        public void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    out[((z ^ mask) << shift) | x] = in[(x << shift) | z];
                }
            }
        }
    },
    ANY(4) {
        @Override
        public void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r) {
            VALUES[r].rotate(in, out, size, mask, shift, 0);
        }
    };

    private static final RotationMode[] VALUES = values();

    public final int rounds;

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
    public abstract void rotate(@NonNull byte[] in, @NonNull byte[] out, int size, int mask, int shift, int r);
}
