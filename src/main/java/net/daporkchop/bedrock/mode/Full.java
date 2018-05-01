package net.daporkchop.bedrock.mode;

import net.daporkchop.bedrock.Bedrock;
import net.daporkchop.bedrock.Callback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author DaPorkchop_
 */
public class Full {
    public static boolean chunk_match(byte[] c, long x, long z) {
        Bedrock.processedChunks.incrementAndGet();
        long seed = ((
                (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL)
                * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

        for (int a = 0; a < 16; ++a) {
            for (int b = 0; b < 16; ++b) {
                byte v = c[a * 16 + b];

                if (v != Bedrock.WILDCARD) {
                    if (4 <= (seed >> 17) % 5) {
                        if (v != 1) {
                            return false;
                        }
                    } else {
                        if (v != 0) {
                            return false;
                        }
                    }
                }

                seed = ((seed * 5985058416696778513L + -8542997297661424380L) * 709490313259657689L + 1748772144486964054L) & 281474976710655L;
            }
        }

        return true;
    }

    public static void bedrock_finder_fullpattern(byte[] pattern, int id, int step, int start, int end, Callback callback, AtomicBoolean running) {
        for (int r = start + id; r <= end; r += step) {
            for (int i = -r; i <= r; i++) {
                if (chunk_match(pattern, i, r)) {
                    callback.onComplete(i << 4, r << 4);
                }
                if (chunk_match(pattern, i, -r)) {
                    callback.onComplete(i << 4, (-r) << 4);
                }
            }
            /*for (int i = -r + 1; i < r; i++) {
                if (chunk_match(pattern, r, i)) {
                    callback.onComplete(i << 4, r << 4);
                }
                if(chunk_match(pattern, i, -r)) {
                    callback.onComplete(i << 4, (-r) << 4);
                }
            }*/
            //TODO: figure out why this is needed
            if (!running.get()) {
                return;
            }
        }
    }
}
