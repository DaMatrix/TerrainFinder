package net.daporkchop.bedrock.mode;

import net.daporkchop.bedrock.Bedrock;

/**
 * @author DaPorkchop_
 */
public class Sub {
    public static boolean sub_chunk_match(byte[] sub, char rows, char cols, long x, long z) {
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;
        byte[] chunk = Bedrock.chunkPattern.get();

        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                seed = (seed * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

                if (4 <= (seed >> 17) % 5) {
                    chunk[a * 16 + b] = 1;
                } else {
                    chunk[a * 16 + b] = 0;
                }

                seed = seed * 5985058416696778513L + -8542997297661424380L;
            }
        }

        boolean match;
        for (int m = 0; m <= 16 - rows; m++) {
            for (int n = 0; n <= 16 - cols; n++) {
                match = true;
                for (int i = 0; match && i < rows; i++) {
                    for (int j = 0; match && j < cols; j++) {
                        if (sub[i * cols + j] != chunk[(m + i) * 16 + (n + j)]) {
                            match = false;
                        }
                    }
                }
                if (match) {
                    Bedrock.sub_match_x = m;
                    Bedrock.sub_match_z = n;

                    return true;
                }
            }
        }

        return false;
    }
}
