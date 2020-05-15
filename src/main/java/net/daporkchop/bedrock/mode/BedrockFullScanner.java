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
 * Scans an entire 16x16 chunk for a 16x16 pattern
 *
 * @author DaPorkchop_
 */
@SuppressWarnings("Duplicates")
public class BedrockFullScanner implements TileScanner {
    protected final byte[][] patterns;

    public BedrockFullScanner(@NonNull byte[] pattern, @NonNull Rotation rotation) {
        this.patterns = rotation.bake(pattern, 16, 0xF, 4);
    }

    @Override
    public long scan(int tileX, int tileZ) {
        long bits = 0L;
        final byte[][] patterns = this.patterns;
        final int numPatterns = patterns.length;

        for (int subX = 0; subX < TILE_SIZE; subX++) {
            for (int subZ = 0; subZ < TILE_SIZE; subZ++) {
                final long seed = seedBedrock((tileX << TILE_SHIFT) | subX, (tileZ << TILE_SHIFT) | subZ);
                PATTERN:
                for (int patternIndex = 0; patternIndex < numPatterns; patternIndex++) {
                    byte[] pattern = patterns[patternIndex];
                    long state = seed;

                    for (int i = 0; i < 256; i++) {
                        byte v = pattern[i];

                        //~110 million/s (wildcards)
                        //~106 million/s (wildcards, no sign extension (why?))
                        //~109.5 million/s (no wildcards)
                        if (!ALLOW_WILDCARDS || v != WILDCARD) {
                            if (4 == (state >> 17) % 5) {
                                if (v == 0) {
                                    continue PATTERN;
                                }
                            } else {
                                if (v == 1) {
                                    continue PATTERN;
                                }
                            }
                        }

                        //~107 million/s (wildcards)
                        //~100 million/s (no wildcards)
                        /*if ((!ALLOW_WILDCARDS || v != WILDCARD) && v != flagBedrock(state)) {
                            continue PATTERN;
                        }*/

                        state = updateBedrock(state);
                    }

                    bits |= 1L << ((subX << TILE_SHIFT) | subZ);
                    break;
                }
            }
        }
        return bits;
    }
}
