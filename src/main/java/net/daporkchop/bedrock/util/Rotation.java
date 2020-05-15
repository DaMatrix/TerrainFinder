/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.bedrock.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.util.PArrays;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
public enum Rotation {
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

    private static final Rotation[] VALUES = values();

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

    /**
     * Generates all permutations of the given pattern
     *
     * @param pattern the input pattern
     * @param size    the size (N by N) of the tile to rotate
     * @param mask    the bitmask of the size
     * @param shift   the number of bits to shift the X coordinate by
     * @return all permutations of the pattern
     */
    public byte[][] bake(@NonNull byte[] pattern, int size, int mask, int shift) {
        return PArrays.filled(this.rounds, byte[][]::new, i -> {
            byte[] arr = new byte[pattern.length];
            this.rotate(pattern, arr, 16, 0xF, 4, i);
            return arr;
        });
    }
}
