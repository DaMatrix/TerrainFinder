package net.daporkchop.bedrock.util;

import lombok.NonNull;
import net.daporkchop.bedrock.util.func.VoidFunction;

/**
 * An async task that knows when it completes
 *
 * @author DaPorkchop_
 */
public class AsyncTask extends Thread {
    /**
     * The constructor to execute
     */
    @NonNull
    private final VoidFunction function;

    /**
     * Whether or not this task is complete
     */
    public volatile boolean complete = false;

    public AsyncTask(@NonNull VoidFunction function) {
        super();
        this.function = function;
        super.start();
    }

    public AsyncTask(String name, @NonNull VoidFunction function) {
        super(name);
        this.function = function;
        super.start();
    }

    @Override
    public void run() {
        this.function.run();
        this.complete = true;
    }

    @Override
    public synchronized void start() {
        throw new UnsupportedOperationException();
    }
}
