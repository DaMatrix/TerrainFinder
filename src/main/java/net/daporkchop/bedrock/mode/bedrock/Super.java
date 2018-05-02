package net.daporkchop.bedrock.mode.bedrock;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Searches for an 8x8 pattern that can overlap into neighboring chunks
 *
 * @author DaPorkchop_
 */
public class Super extends BedrockAlg {
    private static final ThreadLocal<byte[]> chunkPattern_super = ThreadLocal.withInitial(() -> new byte[48 * 48]);

    public Super(@NonNull AtomicLong processed, @NonNull byte[] pattern, @NonNull Callback callback) {
        super(processed, pattern, callback);
    }

    public static byte[] fill3x3(int x, int z) {
        byte[] bchunk = chunkPattern_super.get();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                long seed = ((
                        ((x + j) * 341873128712L + (z + i) * 132897987541L) ^ 0x5DEECE66DL
                ) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
                for (int a = 0; a < 16; ++a) {
                    for (int b = 0; b < 16; ++b) {
                        seed = seed & ((1L << 48L) - 1L);

                        if (4 <= (seed >> 17) % 5) {
                            bchunk[(16 * (i + 1) + a) * 48 + (16 * (j + 1) + b)] = 1;
                        } else {
                            bchunk[(16 * (i + 1) + a) * 48 + (16 * (j + 1) + b)] = 0;
                        }

                        seed = ((seed * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
                    }
                }
            }
        }

        return bchunk;
    }

    @Override
    protected boolean scan(int x, int z) {
        this.processed.incrementAndGet();
        byte[] bchunk = fill3x3(x, z);

        boolean match;
        for (int m = 0; m <= 40; m++) {
            for (int n = 0; n <= 40; n++) {
                match = true;
                for (int i = 0; match && i < 8; i++) {
                    for (int j = 0; match && j < 8; j++) {
                        byte v = pattern[(i << 3) + j];
                        if (v != WILDCARD && v != bchunk[(m + i) * 48 + (n + j)]) {
                            match = false;
                        }
                    }
                }
                if (match) {
                    return true;
                }
            }
        }

        return false;
    }
}
