package net.daporkchop.bedrock.mode;

import lombok.NonNull;
import net.daporkchop.bedrock.util.RotationMode;
import net.daporkchop.bedrock.util.TileScanner;
import net.daporkchop.lib.common.util.PArrays;

import static net.daporkchop.bedrock.util.BedrockConstants.*;

/**
 * Scans an entire 16x16 chunk for a 16x16 pattern
 *
 * @author DaPorkchop_
 */
public class BedrockFullScanner implements TileScanner {
    protected final byte[][] patterns;

    public BedrockFullScanner(@NonNull byte[] pattern, @NonNull RotationMode rotation) {
        this.patterns = PArrays.filled(rotation.rounds, byte[][]::new, i -> {
            byte[] arr = new byte[pattern.length];
            rotation.rotate(pattern, arr, 16, 0xF, 4, i);
            return arr;
        });
    }

    @Override
    public int scan(int tileX, int tileZ) {
        int bits = 0;
        final byte[][] patterns = this.patterns;
        final int numPatterns = patterns.length;

        for (int subX = 0; subX < TILE_SIZE; subX++) {
            for (int subZ = 0; subZ < TILE_SIZE; subZ++) {
                final long seed = ((
                        (((tileX << TILE_BITS) | subX) * 341873128712L + ((tileZ << TILE_BITS) | subZ) * 132897987541L) ^ 0x5DEECE66DL)
                        * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
                PATTERN:
                for (int patternIndex = 0; patternIndex < numPatterns; patternIndex++) {
                    byte[] pattern = patterns[patternIndex];
                    long state = seed;

                    for (int i = 0; i < 16 * 16; i++) {
                        byte v = pattern[i];

                        if (!ALLOW_WILDCARDS || v != WILDCARD) {
                            if (4 <= (state >> 17) % 5) {
                                if (v == 0) {
                                    continue PATTERN;
                                }
                            } else {
                                if (v == 1) {
                                    continue PATTERN;
                                }
                            }
                        }

                        state = ((state * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
                    }

                    bits |= 1 << ((subX << TILE_BITS) | subZ);
                    break;
                }
            }
        }
        return bits;
        /*MAIN:
        for (int i = 0; i < this.patterns.length; i++) {
            byte[] pattern = this.patterns[i];
            long seed = ((
                    (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL)
                    * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

            for (int a = 0; a < 16; ++a) {
                for (int b = 0; b < 16; ++b) {
                    byte v = pattern[a * 16 + b];

                    if (v != WILDCARD) {
                        if (4 <= (seed >> 17) % 5) {
                            if (v == 0) {
                                continue MAIN;
                            }
                        } else {
                            if (v == 1) {
                                continue MAIN;
                            }
                        }
                    }

                    seed = ((seed * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
                }
            }

            return true;
        }

        return false;*/
    }
}
