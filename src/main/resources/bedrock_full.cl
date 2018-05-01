__kernel void sampleKernel(__global const int *xIn, __global const int *zIn, __global int *out, __global int *pattern) {
    int gid = get_global_id(0);
    int v;
    long seed = (xIn[gid] * 341873128712L + zIn[gid] * 132897987541L)^0x5DEECE66DL;
    printf("%d\n", xIn[gid]);

    for (int a = 0; a < 16; ++a) {
        for (int b = 0; b < 16; ++b) {
            seed = seed*709490313259657689L + 1748772144486964054L;
            seed = seed & ((1L << 48L) - 1L);

            v = pattern[a * 16 + b];

            if (v != 2) {
                if (4 <= (seed >> 17) % 5) {
                    if (v != 1) {
                        out[gid] = 1;
                        return;
                    }
                } else {
                    if (v != 0) {
                        out[gid] = 1;
                        return;
                    }
                }
            }

            seed = seed*5985058416696778513L + -8542997297661424380L;
        }
    }
    out[gid] = 0;
}
