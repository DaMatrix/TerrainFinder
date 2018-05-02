package net.daporkchop.bedrock;

import lombok.NonNull;
import net.daporkchop.bedrock.gui.BedrockDialog;
import net.daporkchop.bedrock.mode.bedrock.BedrockAlg;
import net.daporkchop.bedrock.mode.bedrock.BedrockMode;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static String[] args;

    public Bedrock(int threads, String mode, LongConsumer update, long updateInterval, Callback callback) {
        this(null, threads, BedrockMode.valueOf(mode.toUpperCase()), update, updateInterval, callback);
    }

    public Bedrock(byte[] pattern, int threads, @NonNull BedrockMode mode, LongConsumer update, long updateInterval, Callback callback) {
        if (pattern == null) {
            pattern = mode.def;
        }

        final Set<Thread> workers = new HashSet<>();

        {
            final Callback ree = callback;

            callback = (x, z, p) ->
                    new Thread() {
                        @Override
                        public void run() {
                            workers.forEach(Thread::stop);
                            ree.onComplete(x, z, p);
                            update.accept(0);
                        }
                    }.start();
        }

        final BedrockAlg alg = mode.function.newInstance(new AtomicLong(0), pattern, callback);

        {
            for (int i = 0; i < threads; i++) {
                final int REEE = i;
                workers.add(new Thread("Bedrock scanner #" + i) {
                    @Override
                    public void run() {
                        alg.doSearch(REEE, threads);
                    }
                });
            }
            workers.forEach(Thread::start);
        }

        while (alg.isRunning()) {
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            update.accept(alg.getProcessed().get());
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
                (x, z, p) -> {
                    System.out.println("Found match at x=" + x + ", z=" + z);
                    System.out.println("after scanning " + p + " chunks");
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
