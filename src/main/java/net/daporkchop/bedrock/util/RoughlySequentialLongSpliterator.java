package net.daporkchop.bedrock.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.function.LongConsumer;

import static java.lang.Math.*;
import static net.daporkchop.bedrock.util.UnorderedLongSpliterator.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A spliterator that provides more or less sequential iteration over a range of {@code long}s.
 * <p>
 * Need I say more?
 *
 * @author DaPorkchop_
 */
public class RoughlySequentialLongSpliterator implements Spliterator.OfLong {
    protected static final long BATCH_SIZE = 1024L << 8L;

    protected long pos;
    protected final long end;
    protected final long batchSize;

    public RoughlySequentialLongSpliterator(long startInclusive, long endExclusive) {
        this(startInclusive, endExclusive, ForkJoinPool.getCommonPoolParallelism());
    }

    public RoughlySequentialLongSpliterator(long startInclusive, long endExclusive, int parallelism) {
        checkArg(startInclusive <= endExclusive, "start (%d) may not be less than end (%d)!", startInclusive, endExclusive);
        this.pos = startInclusive;
        this.end = endExclusive;
        this.batchSize = positive(parallelism, "parallelism") * BATCH_SIZE;
    }

    @Override
    public OfLong trySplit() {
        long effectiveBatchSize = min(this.batchSize, this.estimateSize() >> 1L);
        if (effectiveBatchSize < MIN_BATCH) {
            return null;
        }
        long oldPos = this.pos;
        this.pos = oldPos + effectiveBatchSize;
        return new UnorderedLongSpliterator(oldPos, this.pos);
    }

    @Override
    public boolean tryAdvance(LongConsumer action) {
        if (this.pos < this.end) {
            action.accept(this.pos++);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public long estimateSize() {
        return this.end - this.pos;
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
    }
}
