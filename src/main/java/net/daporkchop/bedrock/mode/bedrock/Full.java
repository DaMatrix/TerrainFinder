package net.daporkchop.bedrock.mode.bedrock;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.util.RotationMode;
import net.daporkchop.lib.unsafe.PUnsafe;

/**
 * Scans an entire 16x16 chunk for a 16x16 pattern
 *
 * @author DaPorkchop_
 */
public class Full extends BedrockAlg {
    public Full(@NonNull byte[] pattern, @NonNull Callback callback, @NonNull RotationMode rotation, int threads) {
        super(pattern, callback, rotation, threads);
    }

    @Override
    protected boolean scan(int x, int z) {
        PUnsafe.getAndAddLong(this, PROCESSED_OFFSET, 1L);
        MAIN:
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

        return false;
    }

    @Override
    protected int getSize() {
        return 0x10;
    }

    @Override
    protected int getMask() {
        return 0xF;
    }

    @Override
    protected int getShift() {
        return 4;
    }
}
