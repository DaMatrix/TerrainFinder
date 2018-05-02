package net.daporkchop.bedrock.mode.bedrock;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Scans an entire 16x16 chunk for a 16x16 pattern
 *
 * @author DaPorkchop_
 */
public class Full extends BedrockAlg {
    //TODO: figure out if lombok can do this constructor for me
    public Full(@NonNull AtomicLong processed, @NonNull byte[] pattern, @NonNull Callback callback, int threads) {
        super(processed, pattern, callback, threads);
    }

    @Override
    protected boolean scan(int x, int z) {
        this.processed.incrementAndGet();
        long seed = ((
                (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL)
                * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

        for (int a = 0; a < 16; ++a) {
            for (int b = 0; b < 16; ++b) {
                byte v = pattern[a * 16 + b];

                if (v != WILDCARD) {
                    if (4 <= (seed >> 17) % 5) {
                        if (v == 0) {
                            return false;
                        }
                    } else {
                        if (v == 1) {
                            return false;
                        }
                    }
                }

                seed = ((seed * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
            }
        }

        return true;
    }
}
