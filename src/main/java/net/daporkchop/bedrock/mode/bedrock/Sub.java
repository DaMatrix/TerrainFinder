package net.daporkchop.bedrock.mode.bedrock;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Scans for an 8x8 pattern contained in a single chunk
 *
 * @author DaPorkchop_
 */
public class Sub extends BedrockAlg {
    private final ThreadLocal<byte[]> chunkPattern = ThreadLocal.withInitial(() -> new byte[256]);

    public Sub(@NonNull AtomicLong processed, @NonNull byte[] pattern, @NonNull Callback callback, int threads) {
        super(processed, pattern, callback, threads);
    }

    @Override
    protected boolean scan(int x, int z) {
        this.processed.incrementAndGet();
        long seed = ((
                (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL)
                * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
        byte[] chunk = chunkPattern.get();

        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                if (4 <= (seed >> 17) % 5) {
                    chunk[a * 16 + b] = 1;
                } else {
                    chunk[a * 16 + b] = 0;
                }

                seed = ((seed * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
            }
        }

        boolean match;
        for (int m = 0; m <= 8; m++) {
            for (int n = 0; n <= 8; n++) {
                match = true;
                for (int i = 0; match && i < 8; i++) {
                    for (int j = 0; match && j < 8; j++) {
                        byte v = pattern[(i << 3) + j];
                        if (v != WILDCARD && v != chunk[((m + i) << 4) | (n + j)]) {
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