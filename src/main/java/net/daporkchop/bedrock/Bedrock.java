package net.daporkchop.bedrock;

import net.daporkchop.bedrock.gui.BedrockDialog;
import net.daporkchop.bedrock.mode.Modes;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static final boolean WILDCARDS = false;

    public static final byte WILDCARD = 2;
    public static final ThreadLocal<byte[]> chunkPattern = ThreadLocal.withInitial(() -> new byte[256]);
    public static final ThreadLocal<byte[]> chunkPattern_super = ThreadLocal.withInitial(() -> new byte[48 * 48]);
    public static final AtomicLong processedChunks = new AtomicLong(0L);
    public static int sub_match_x = 0;
    public static int sub_match_z = 0;
    public static String[] args;

    public Bedrock(int threads, String mode, LongConsumer update, long updateInterval, Callback callback) {
        this(null, threads, Modes.valueOf(mode.toUpperCase()), update, updateInterval, callback);
    }

    public Bedrock(byte[] pattern, int threads, Modes mode, LongConsumer update, long updateInterval, Callback callback) {
        if (mode == null) {
            throw new IllegalArgumentException("Invalid mode!");
        }
        if (pattern == null) {
            pattern = mode.def;
        }

        final Set<Thread> workers = new HashSet<>();
        AtomicBoolean running = new AtomicBoolean(true);

        {
            final Callback ree = callback;

            callback = (x, z) -> new Thread() {
                @Override
                public void run() {
                    running.set(false);
                    workers.forEach(Thread::stop);
                    ree.onComplete(x, z);
                    processedChunks.set(0);
                    update.accept(0);
                }
            }.start();
        }

        {
            final Callback cbk = callback;
            final byte[] pat = pattern;

            for (int i = 0; i < threads; i++) {
                final int REEE = i;
                workers.add(new Thread("Bedrock scanner #" + i) {
                    @Override
                    public void run() {
                        mode.function.run(pat, REEE, threads, 0, 1875000, cbk, running);
                    }
                });
            }
            workers.forEach(Thread::start);
        }

        while (running.get()) {
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            update.accept(processedChunks.get());
        }
    }

    public static void main(String[] args) {
        Bedrock.args = args;

        String mode = getArg(0, "gui");
        switch (mode) {
            case "gui":
                BedrockDialog.main(args);
                return;
            case "help":
            case "--help":
            case "-h":
            case "-help":
                System.out.println("Usage:\n" +
                        "java -jar bedrockscanner.jar [mode] [threads]\n" +
                        "\n" +
                        "Modes:\n" +
                        "  full\n" +
                        "  sub\n" +
                        "  super\n" +
                        "  gui");
                return;
        }

        new Bedrock(getArgI(1, Runtime.getRuntime().availableProcessors()), mode,
                l -> System.out.println("Processed " + BedrockDialog.numberFormat.format(l) + " chunks"), 10000L,
                (x, z) -> {
                    System.out.println("Found match at x=" + x + ", z=" + z);
                    System.out.println("after scanning " + processedChunks.get() + " chunks");
                    System.exit(0);
                });
    }

    public static int getArgI(int index, int def) {
        return index >= args.length ? def : Integer.parseInt(args[index]);
    }

    public static String getArg(int index, String def) {
        return index >= args.length ? def : args[index];
    }
}
