package net.daporkchop.bedrock.mode.bedrock;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.util.RotationMode;
import net.daporkchop.lib.unsafe.PUnsafe;

import java.util.concurrent.CountDownLatch;

/**
 * @author DaPorkchop_
 */
@Getter
@Setter
public abstract class BedrockAlg {
    protected static final long PROCESSED_OFFSET = PUnsafe.pork_getOffset(BedrockAlg.class, "processed");

    /**
     * The maximum search radius. This default value (1875000) will scan the whole world.
     */
    public static final int END = 1875000;

    /**
     * This value implies a wildcard (when the user doesn't know if there's
     * bedrock at a given position)
     */
    public static final byte WILDCARD = 2;

    /**
     * A counter for processed chunks. Incremented every time a chunk is scanned
     */
    @Setter(AccessLevel.NONE)
    protected volatile long processed = 0L;

    /**
     * A constructor that will be invoked when a match is found.
     * By default this should somehow or other also terminate the workers.
     */
    @NonNull
    protected final Callback callback;

    /**
     * The rotation mode to use
     */
    @NonNull
    protected final RotationMode rotation;

    /**
     * All worker threads
     */
    @Setter(AccessLevel.PRIVATE)
    protected volatile Thread[] tasks;

    /**
     * The real pattern, with rotations (if any)
     */
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    protected final byte[][] patterns;

    protected final CountDownLatch latch;

    /**
     * The number of threads to start
     */
    protected final int threads;

    /**
     * Whether or not this algorithm is running. When set to false, the algorithm should stop searching.
     */
    @Setter(AccessLevel.PRIVATE)
    protected volatile boolean running = true;

    public BedrockAlg(@NonNull byte[] pattern, @NonNull Callback callback, @NonNull RotationMode rotation, int threads) {
        this.callback = callback;
        this.rotation = rotation;
        this.threads = threads;
        this.latch = new CountDownLatch(threads);
        this.tasks = new Thread[threads];

        this.patterns = new byte[this.rotation.rounds][pattern.length];
        for (int i = 0; i < this.patterns.length; i++) {
            this.rotation.rotate(pattern, this.patterns[i], this.getSize(), this.getMask(), this.getShift(), i);
        }
        for (int i = 0; i < this.threads; i++) {
            //"Variable 'i' is accessed from inner class, must be final or effectively final"
            //dammit java this has been an issue forever
            final int id = i;
            this.tasks[i] = new Thread(() -> this.doSearch(id), "Bedrock search worker #" + i);
        }
        for (int i = 0; i < this.threads; i++) {
            this.tasks[i].start();
        }
    }

    public void await() {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the search
     *
     * @param blocking whether or not to wait for worker threads to exit
     */
    public void stop(boolean blocking) {
        this.running = false;
        for (Thread thread : this.tasks) {
            thread.interrupt();
        }
        if (blocking) {
            this.await();
        }
    }

    /**
     * Runs a full search
     *
     * @param id the id of this worker thread (0-max)
     */
    protected void doSearch(int id) {
        for (int r = id; r <= END; r += this.threads) {
            for (int i = -r; i <= r; i++) {
                if (this.scan(i, r)) {
                    this.onMatch(i, r);
                }
                if (this.scan(i, -r)) {
                    this.onMatch(i, -r);
                }
                if (Thread.interrupted() && !this.running) {
                    return;
                }
            }
        }
    }

    /**
     * Called when a matching chunk is found
     *
     * @param x the chunk's x coordinate
     * @param z the chunk's z coordinate
     */
    protected void onMatch(int x, int z) {
        if (this.callback.onComplete(x << 4, z << 4, this.processed)) {
            this.stop(false);
        }
    }

    /**
     * Scans a given chunk
     *
     * @param x the chunk's x coordinate
     * @param z the chunk's z coordinate
     * @return whether or not the given chunk is a match
     */
    protected abstract boolean scan(int x, int z);

    protected abstract int getSize();

    protected abstract int getMask();

    protected abstract int getShift();
}
