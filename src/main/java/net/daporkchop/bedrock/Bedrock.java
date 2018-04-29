package net.daporkchop.bedrock;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static final boolean WILDCARDS = false;
    public static final boolean DEBUG = false;
    public static final byte[] fuL_pattern = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0,
            1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
            1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0,
            1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
            0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0,
            0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1,
            0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1,
            1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0,
            0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1,
            0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0,
            0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0
    };
    public static final byte[] sub_pattern = new byte[]{
            0, 0, 1, 0, 0, 0, 1, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 1, 1, 0, 0, 0, 0,
            0, 1, 1, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 1, 0, 1,
            1, 0, 0, 0, 1, 0, 0, 0,
            0, 0, 1, 0, 0, 1, 0, 1
    };
    public static final byte[] any_pattern = new byte[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 0, 0, 0, 0,
            0, 1, 1, 0, 0, 0, 0, 0,
            0, 0, 0, 1, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 1, 0, 0, 1, 0, 0
    };
    private static final byte WILDCARD = 2;
    private static final ThreadLocal<byte[]> chunkPattern = ThreadLocal.withInitial(() -> new byte[256]);
    private static final ThreadLocal<byte[]> chunkPattern_super = ThreadLocal.withInitial(() -> new byte[48 * 48]);
    private static int sub_match_x = 0;
    private static int sub_match_z = 0;

    public static boolean chunk_match(byte[] c, long x, long z) {
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;

        for (int a = 0; a < 16; ++a) {
            for (int b = 0; b < 16; ++b) {
                seed = seed * 709490313259657689L + 1748772144486964054L;

                seed = seed & ((1L << 48L) - 1L);

                byte v = c[a * 16 + b];

                A:
                {
                    if (WILDCARDS && v != WILDCARD) {
                        break A;
                    } else if (4 <= (seed >> 17) % 5) {
                        if (c[a * 16 + b] != 1)
                            // if a comparison fails, bail out
                            return false;
                    } else {
                        if (c[a * 16 + b] != 0)
                            return false;
                    }
                }

                seed = seed * 5985058416696778513L + -8542997297661424380L;
            }
        }

        return true;
    }

    public static boolean sub_chunk_match(byte[] sub, char rows, char cols, long x, long z) {
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;
        byte[] chunk = chunkPattern.get();

        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                seed = seed * 709490313259657689L + 1748772144486964054L;
                seed = seed & ((1L << 48L) - 1L); //TODO: optimize this

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
                for (int i = 0; i < rows && match == true; i++) {
                    for (int j = 0; j < cols && match == true; j++) {
                        if (sub[i * cols + j] != chunk[(m + i) * 16 + (n + j)]) {
                            match = false;
                        }
                    }
                }
                if (match) {
                    sub_match_x = m;
                    sub_match_z = n;

                    return true;
                }
            }
        }

        return false;
    }

    public static boolean super_sub_chunk_match(byte[] sub, char rows, char cols, long x, long z) {
        byte[] bchunk = chunkPattern_super.get();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                long seed = ((x + j) * 341873128712L + (z + i) * 132897987541L) ^ 0x5DEECE66DL;
                for (int a = 0; a < 16; ++a) {
                    for (int b = 0; b < 16; ++b) {
                        seed = seed * 709490313259657689L + 1748772144486964054L;
                        seed = seed & ((1L << 48L) - 1L);

                        if (4 <= (seed >> 17) % 5) {
                            bchunk[(16 * (i + 1) + a) * 48 + (16 * (j + 1) + b)] = 1;
                        } else {
                            bchunk[(16 * (i + 1) + a) * 48 + (16 * (j + 1) + b)] = 0;
                        }

                        seed = seed * 5985058416696778513L + -8542997297661424380L;
                    }
                }
            }
        }

        boolean match;
        for (int m = 0; m <= 48 - rows; m++) {
            for (int n = 0; n <= 48 - cols; n++) {
                match = true;
                for (int i = 0; i < rows && match == true; i++) {
                    for (int j = 0; j < cols && match == true; j++) {
                        if (sub[i * cols + j] != bchunk[(m + i) * 48 + (n + j)]) {
                            match = false;
                        }
                    }
                }
                if (match) {
                    sub_match_x = m;
                    sub_match_z = n;

                    return true;
                }
            }
        }

        return false;
    }
}
