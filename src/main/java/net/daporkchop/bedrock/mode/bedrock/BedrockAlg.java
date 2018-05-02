package net.daporkchop.bedrock.mode.bedrock;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.util.AsyncTask;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author DaPorkchop_
 */
@Data
public abstract class BedrockAlg {
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
    @NonNull
    protected final AtomicLong processed;

    /**
     * The pattern we're scanning for.
     * Indexed as in:
     * (x << 4) | z
     * where 0 <= x, z <= 15
     */
    @NonNull
    protected final byte[] pattern;

    /**
     * A constructor that will be invoked when a match is found.
     * By default this should somehow or other also terminate the workers
     * //TODO: automatic worker termination in this class
     */
    @NonNull
    protected final Callback callback;

    /**
     * The number of threads to start
     */
    protected final int threads;

    /**
     * Whether or not this algorithm is running. When set to false, the algorithm should stop searching.
     */
    @Setter(AccessLevel.PRIVATE)
    protected transient volatile boolean running = false;

    /**
     * All worker threads
     */
    @Setter(AccessLevel.PRIVATE)
    protected transient volatile AsyncTask[] tasks;

    /**
     * Starts the search algorithm on the number of threads specified
     * in the constructor
     *
     * @param blocking if true, this will block the invoking thread until the search is complete. if not,
     *                 it will start the worker threads and return
     * @return if blocking it will return true when search is complete (unless an error occurs, in which case it returns false), if not blocking it will return false
     */
    public synchronized boolean start(boolean blocking) {
        if (this.running) {
            throw new ConcurrentModificationException("Cannot start search while another one is running");
        } else {
            this.running = true;
        }

        if (tasks == null) {
            tasks = new AsyncTask[this.threads];
        }
        for (int i = 0; i < this.threads; i++) {
            //"Variable 'i' is accessed from inner class, must be final or effectively final"
            //dammit java this has been an issue forever
            final int id = i;
            this.tasks[i] = new AsyncTask("Bedrock search worker #" + i, () -> doSearch(id));
        }

        if (blocking) {
            try {
                while (running) {
                    Thread.sleep(25);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Stops the search
     *
     * @param blocking whether or not to wait for worker threads to exit
     */
    public void stop(boolean blocking) {
        this.running = false;
        if (blocking) {
            try {
                while (running) {
                    Thread.sleep(25);
                    boolean running = false;
                    for (AsyncTask task : this.tasks) {
                        running |= !task.complete;
                    }
                    if (!running) {
                        return;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Runs a full search
     *
     * @param id the id of this worker thread (0-max)
     */
    protected void doSearch(int id) {
        for (int r = id; running && r <= END; r += this.threads) {
            for (int i = -r; running && i <= r; i++) {
                if (scan(i, r)) {
                    onMatch(i, r);
                }
                if (scan(i, -r)) {
                    onMatch(i, -r);
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
        running = false;
        callback.onComplete(x << 4, z << 4, this.processed.get());
    }

    /**
     * Scans a given chunk
     *
     * @param x the chunk's x coordinate
     * @param z the chunk's z coordinate
     * @return whether or not the given chunk is a match
     */
    protected abstract boolean scan(int x, int z);
}
