package net.daporkchop.bedrock.mode;

import net.daporkchop.bedrock.Callback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author DaPorkchop_
 */
public interface ISearchMode {
    void run(byte[] pattern, int id, int step, int start, int end, Callback callback, AtomicBoolean running);
}
