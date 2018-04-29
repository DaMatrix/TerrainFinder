package net.daporkchop.bedrock;

import net.daporkchop.bedrock.mode.Full;
import net.daporkchop.bedrock.mode.ISearchMode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static final boolean WILDCARDS = false;

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
    public static final byte WILDCARD = 2;
    public static final ThreadLocal<byte[]> chunkPattern = ThreadLocal.withInitial(() -> new byte[256]);
    public static final ThreadLocal<byte[]> chunkPattern_super = ThreadLocal.withInitial(() -> new byte[48 * 48]);
    public static final AtomicInteger processedChunks = new AtomicInteger(0);
    public static int sub_match_x = 0;
    public static int sub_match_z = 0;
    public static String[] args;

    public static void main(String... args) {
        Bedrock.args = args;

        final int threads = getArgI(0, Runtime.getRuntime().availableProcessors());
        final ISearchMode mode;

        {
            ISearchMode REEE = null;
            final String modeText = getArg(1, "full");
            switch (modeText) {
                case "full":
                    REEE = Full::bedrock_finder_fullpattern;
                    break;
                case "sub":
                case "any":
                default:
                    throw new UnsupportedOperationException("Unimplemented mode: " + modeText);
            }
            mode = REEE;
        }

        for (int i = 0; i < threads; i++) {
            final int REEE = i;
            new Thread("Bedrock scanner #" + i) {
                @Override
                public void run() {
                    mode.run(full_pattern, REEE, threads, 0, 1875000);
                }
            }.start();
        }

        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Scanned " + processedChunks.get() + " chunks...");
        }
    }

    public static int getArgI(int index, int def) {
        return index >= args.length ? def : Integer.parseInt(args[index]);
    }

    public static String getArg(int index, String def) {
        return index >= args.length ? def : args[index];
    }

    public static void close(int x, int z) {
        System.out.println("Found pattern at x=" + x + ", z=" + z);
        System.out.println("after scanning " + processedChunks.get() + " chunks");
        System.exit(0);
    }
}
