package net.daporkchop.bedrock.util;

import net.daporkchop.lib.unsafe.PUnsafe;

import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.function.LongConsumer;

import static java.lang.Math.*;
import static net.daporkchop.bedrock.util.UnorderedLongSpliterator.*;
import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * A spliterator that provides sequential iteration over a range of {@code long}s by utilizing a shared atomic counter.
 *
 * @author DaPorkchop_
 */
public final class AtomicallySequentialLongSpliterator implements OfLong {
    protected static final long POS_OFFSET = PUnsafe.pork_getOffset(AtomicallySequentialLongSpliterator.class, "pos");

    protected volatile long pos;
    protected final long end;

    public AtomicallySequentialLongSpliterator(long startInclusive, long endExclusive) {
        checkArg(startInclusive <= endExclusive, "start (%d) may not be less than end (%d)!", startInclusive, endExclusive);
        this.pos = startInclusive;
        this.end = endExclusive;
    }

    @Override
    public OfLong trySplit() {
        return this;
    }

    @Override
    public boolean tryAdvance(LongConsumer action) {
        long pos;
        do {
            pos = PUnsafe.getLongVolatile(this, POS_OFFSET);
        } while (pos < this.end && PUnsafe.compareAndSwapLong(this, POS_OFFSET, pos, pos + 1L));

        if (pos < this.end) {
            action.accept(pos);
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
        return Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SORTED;
    }
}
