/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2018-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.bedrock;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.daporkchop.bedrock.util.FoundCallback;
import net.daporkchop.bedrock.util.TileFilter;
import net.daporkchop.bedrock.util.TileScanner;
import net.daporkchop.lib.concurrent.PExecutors;
import net.daporkchop.lib.concurrent.PFuture;
import net.daporkchop.lib.concurrent.future.DefaultPFuture;
import net.daporkchop.lib.unsafe.PUnsafe;

import static net.daporkchop.bedrock.util.Constants.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * Represents a search for chunk coordinates that contain something.
 *
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public final class Search implements Runnable {
    private static final long PROCESSED_OFFSET = PUnsafe.pork_getOffset(Search.class, "processed");

    protected volatile long processed = 0L;

    protected final PFuture<Void> completedFuture = new DefaultPFuture<>(PExecutors.FORKJOINPOOL);

    /**
     * Allows rapid skipping of certain tiles.
     * <p>
     * If {@code null}, all tiles will be checked.
     */
    protected final TileFilter filter;

    /**
     * The actual scanner which will scan for chunks inside of a tile that match some condition.
     */
    @NonNull
    protected final TileScanner scanner;

    /**
     * A function to call when a matching chunk has been found.
     */
    @NonNull
    protected final FoundCallback onFound;

    /**
     * Begins searching using the given number of threads.
     * <p>
     * Note that this can be called multiple times to add more and more workers to an ongoing task.
     *
     * @param threads the number of threads to use
     */
    public PFuture<Void> start(int threads) {
        positive(threads, "threads");
        for (int i = 0; i < threads; i++) {
            new Thread(this).start();
        }
        return this.completedFuture;
    }

    /**
     * Runs a search worker.
     * <p>
     * This will not return until the search is completed.
     */
    @Override
    public void run() {
        long pos;
        while (!this.completedFuture.isDone() && (pos = PUnsafe.getAndAddLong(this, PROCESSED_OFFSET, 1L)) < WHOLE_WORLD_CAP) {
            this.doWork(pos);
        }
    }

    private void doWork(long pos) {
        int tileX = extractX(pos);
        int tileZ = extractZ(pos);
        if (this.filter == null || this.filter.test(tileX, tileZ))  {
            int i = this.scanner.scan(tileX, tileZ);
            if (i != 0) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    for (int z = 0; z < TILE_SIZE; z++) {
                        if ((i & (1 << ((x << TILE_SHIFT) | z))) != 0 && !this.onFound.found((tileX << TILE_SHIFT) | x, (tileZ << TILE_SHIFT) | z))   {
                            ((DefaultPFuture<Void>) this.completedFuture).trySuccess(null);
                            return;
                        }
                    }
                }
            }
        }
    }
}
