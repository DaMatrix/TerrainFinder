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
import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.math.BinMath;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class BedrockConstants {
    public static final boolean ALLOW_WILDCARDS = Boolean.parseBoolean(System.getProperty("bedrock.wildcard", "true"));
    public static final byte WILDCARD = 2;

    public static final int TILE_SIZE = 4; //the size of a tile in chunks
    public static final int TILE_BITS = BinMath.getNumBitsNeededFor(TILE_SIZE);

    public static final int WORLD_RADIUS = 30000000 >> 4 >> TILE_BITS; //the radius of the world in tiles
    public static final long WHOLE_WORLD_CAP = ((long) WORLD_RADIUS * WORLD_RADIUS) << 2L;

    public static int extractX(long l) {
        //i'll be able to understand how this works ever again, but that doesn't matter because it works now
        //...right?
        long base = floorL(sqrt(l >> 2L));
        long offset = (l >> 2L) - base * base;
        long val = min(offset, base);
        return (int) (val ^ -(l & 1L));
    }

    public static int extractZ(long l) {
        long base = floorL(sqrt(l >> 2L));
        long offset = (l >> 2L) - base * base;
        long val = offset >= base + 1L ? base - (offset >> 1L) : base;
        return (int) (val ^ -((l >> 1L) & 1L));
    }

    public static long seed(int chunkX, int chunkZ) {
        return (((chunkX * 0x4F9939F508L + chunkZ * 0x1EF1565BD5L) ^ 0x5DEECE66DL) * 0x9D89DAE4D6C29D9L + 0x1844E300013E5B56L) & 0xFFFFFFFFFFFFL;
    }

    public static long update(long state) {
        return ((state * 0x530F32EB772C5F11L + 0x89712D3873C4CD04L) * 0x9D89DAE4D6C29D9L + 0x1844E300013E5B56L) & 0xFFFFFFFFFFFFL;
    }

    public static void fullChunk(@NonNull byte[] dst, int chunkX, int chunkZ) {
        long state = seed(chunkX, chunkZ);

        for (int i = 0; i < 256; i++) {
            dst[i] = (byte) (4 <= (state >> 17) % 5 ? 1 : 0);
            state = update(state);
        }
    }
}
