package net.daporkchop.bedrock.mode.bedrock;

import lombok.Data;
import lombok.NonNull;
import net.daporkchop.bedrock.Callback;

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
     * A function that will be invoked when a match is found.
     * By default this should somehow or other also terminate the workers
     * //TODO: automatic worker termination in this class
     */
    @NonNull
    protected final Callback callback;

    /**
     * Whether or not this algorithm is running. When set to false, the algorithm should stop searching.
     */
    protected transient volatile boolean running = true;

    /**
     * Runs a full search
     *
     * @param id   the id of this worker thread (0-max)
     * @param step the total number of worker threads
     */
    public void doSearch(int id, int step) {
        for (int r = id; running && r <= END; r += step) {
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
