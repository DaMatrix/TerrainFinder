package net.daporkchop.bedrock;

import net.daporkchop.bedrock.mode.Super;

/**
 * @author DaPorkchop_
 */
public class ChunkPrinter {
    public static void print_chunk_pattern(long x, long z) {
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;

        char[] buf = new char[17];
        buf[16] = '\n';

        for (int a = 0; a < 16; ++a) {
            for (int b = 0; b < 16; ++b) {
                seed = seed * 709490313259657689L + 1748772144486964054L;

                seed = seed & ((1L << 48L) - 1L);

                if (4 <= (seed >> 17) % 5) {
                    buf[b] = '*';
                } else {
                    buf[b] = ' ';
                }

                seed = seed * 5985058416696778513L + -8542997297661424380L;
            }
            System.out.print(buf);
        }
    }

    public static void print_super_chunk_pattern(long x, long z) {
        byte[] bchunk = Super.fill3x3(x, z);

        char[] buf = new char[49];
        buf[48] = '\n';

        for (int i = 0; i < 48; i++) {
            for (int j = 0; j < 48; j++) {
                buf[j] = bchunk[i * 48 + j] == 1 ? '*' : ' ';
            }
            System.out.print(buf);
        }
    }
}
