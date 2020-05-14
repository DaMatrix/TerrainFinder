package net.daporkchop.bedrock.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Spliterator;
import java.util.function.LongConsumer;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A spliterator that provides kind of random sequential iteration over a range of {@code long}s.
 *
 * @author DaPorkchop_
 */
public class UnorderedLongSpliterator implements Spliterator.OfLong {
    protected static final long MIN_BATCH = 8L;

    protected long pos;
    protected final long end;

    public UnorderedLongSpliterator(long startInclusive, long endExclusive) {
        checkArg(startInclusive <= endExclusive, "start (%d) may not be less than end (%d)!", startInclusive, endExclusive);
        this.pos = startInclusive;
        this.end = endExclusive;
    }

    @Override
    public OfLong trySplit() {
        long batchSize = this.estimateSize() >> 1L;
        if (batchSize < MIN_BATCH) {
            return null;
        }
        long oldPos = this.pos;
        this.pos = oldPos + batchSize;
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
