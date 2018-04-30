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
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;

        for (int a = 0; a < 16; ++a) {
            for (int b = 0; b < 16; ++b) {
                seed = (seed * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

                byte v = c[a * 16 + b];

                if (Bedrock.WILDCARDS && v != Bedrock.WILDCARD) {
                } else if (4 <= (seed >> 17) % 5) {
                    if (c[a * 16 + b] != 1)
                        // if a comparison fails, bail out
                        return false;
                } else {
                    if (c[a * 16 + b] != 0)
                        return false;
                }

                seed = seed * 5985058416696778513L + -8542997297661424380L;
            }
        }

        return true;
    }

    public static void bedrock_finder_fullpattern(byte[] pattern, int id, int step, int start, int end, Callback callback, AtomicBoolean running) {
        for (int r = start + id; running.get() && r <= end; r += step) {
            for (int i = -r; running.get() && i <= r; i++) {
                if (chunk_match(pattern, i, r)) {
                    callback.onComplete(i << 4, r << 4);
                }
                if (chunk_match(pattern, i, -r)) {
                    callback.onComplete(i << 4, (-r) << 4);
                }
            }
            /*for(int i = -r+1; i < r; i++) {
                if (chunk_match(pattern, r, i)) {
                    System.out.println("chunk: (" + i + ", " + r + "), real: (" + (i << 4) + ", " + (r << 4) + ")");
                }
                if(chunk_match(pattern, i, -r)) {
                    System.out.println("chunk: (" + i + ", " + -r + "), real: (" + (i << 4) + ", " + ((-r) << 4) + ")");
                }
            }*/
            //TODO: figure out what the point of this is
        }
    }
}
