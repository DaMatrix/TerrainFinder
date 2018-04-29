package net.daporkchop.bedrock;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static final boolean WILDCARDS = false;
    public static final boolean DEBUG = false;
    public static final byte[] full_pattern = new byte[]{
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
                seed = (seed * 709490313259657689L + 1748772144486964054L) & 281474976710655L;

                byte v = c[a * 16 + b];

                if (WILDCARDS && v != WILDCARD) {
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

    public static boolean sub_chunk_match(byte[] sub, char rows, char cols, long x, long z) {
        long seed = (x * 341873128712L + z * 132897987541L) ^ 0x5DEECE66DL;
        byte[] chunk = chunkPattern.get();

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
                    sub_match_x = m;
                    sub_match_z = n;

                    return true;
                }
            }
        }

        return false;
    }

    public static boolean super_sub_chunk_match(byte[] sub, char rows, char cols, long x, long z) {
        byte[] bchunk = fill3x3(x, z);

        boolean match;
        for (int m = 0; m <= 48 - rows; m++) {
            for (int n = 0; n <= 48 - cols; n++) {
                match = true;
                for (int i = 0; match && i < rows; i++) {
                    for (int j = 0; match && j < cols; j++) {
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
        byte[] bchunk = fill3x3(x, z);

        char[] buf = new char[49];
        buf[48] = '\n';

        for (int i = 0; i < 48; i++) {
            for (int j = 0; j < 48; j++) {
                buf[j] = bchunk[i * 48 + j] == 1 ? '*' : ' ';
            }
            System.out.print(buf);
        }
    }

    public static byte[] fill3x3(long x, long z) {
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

        return bchunk;
    }

    public static void bedrock_finder_fullpattern(byte[] pattern, int id, int step, int start, int end) {
        for (int r = start + id; r <= end; r += step) {
            for (int i = -r; i <= r; i++) {
                if (chunk_match(pattern, i, r)) {
                    System.out.println("chunk: (" + i + ", " + r + "), real: (" + (i << 4) + ", " + (r << 4) + ")");
                }
                if (chunk_match(pattern, i, -r)) {
                    System.out.println("chunk: (" + i + ", " + -r + "), real: (" + (i << 4) + ", " + ((-r) << 4) + ")");
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

    public static void main(String... args) {
        final int threads = 1;
        for (int i = 0; i < threads; i++) {
            final int REEE = i;
            new Thread("Bedrock scanner #" + i) {
                @Override
                public void run() {
                    bedrock_finder_fullpattern(full_pattern, REEE, threads, 0, 1875000);
                }
            }.start();
        }
    }
}
