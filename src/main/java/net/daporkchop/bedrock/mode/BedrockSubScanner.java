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

package net.daporkchop.bedrock.mode;

import lombok.NonNull;
import net.daporkchop.bedrock.util.Rotation;
import net.daporkchop.bedrock.util.TileScanner;

import static net.daporkchop.bedrock.util.Constants.*;

/**
 * Scans for an 8x8 pattern contained in a single chunk
 *
 * @author DaPorkchop_
 */
@SuppressWarnings("Duplicates")
public class BedrockSubScanner implements TileScanner {
    private static final ThreadLocal<byte[]> CHUNK_CACHE = ThreadLocal.withInitial(() -> new byte[256]);

    protected final byte[][] patterns;

    public BedrockSubScanner(@NonNull byte[] pattern, @NonNull Rotation rotation) {
        this.patterns = rotation.bake(pattern, 8, 7, 3);
    }

    @Override
    public long scan(int tileX, int tileZ) {
        long bits = 0L;
        final byte[][] patterns = this.patterns;
        final int numPatterns = patterns.length;

        final byte[] chunk = CHUNK_CACHE.get();

        for (int subX = 0; subX < TILE_SIZE; subX++) {
            for (int subZ = 0; subZ < TILE_SIZE; subZ++) {
                //generate chunk data
                long state = seedBedrock((tileX << TILE_SHIFT) | subX, (tileZ << TILE_SHIFT) | subZ);
                for (int i = 0; i < 256; i++) {
                    chunk[i] = (byte) flagBedrock(state);
                    state = updateBedrock(state);
                }

                //do search
                PATTERN:
                for (int patternIndex = 0; patternIndex < numPatterns; patternIndex++) {
                    byte[] pattern = patterns[patternIndex];

                    for (int offsetX = 0; offsetX <= 8; offsetX++) {
                        for (int offsetZ = 0; offsetZ <= 8; offsetZ++) {
                            boolean wrong = false;
                            for (int x = 0; !wrong && x < 8; x++) {
                                for (int z = 0; !wrong && z < 8; z++) {
                                    byte v = pattern[(x << 3) | z];
                                    wrong |= (!ALLOW_WILDCARDS || v != WILDCARD) && v != chunk[((offsetX + x) << 4) | (offsetZ + z)];
                                }
                            }

                            if (!wrong) {
                                //if we've gotten this far, a match has been found
                                bits |= 1L << ((subX << TILE_SHIFT) | subZ);
                                break PATTERN;
                            }
                        }
                    }
                }
            }
        }
        return bits;
    }
}
