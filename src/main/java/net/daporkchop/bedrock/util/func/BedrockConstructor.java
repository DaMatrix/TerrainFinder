package net.daporkchop.bedrock.util.func;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.mode.bedrock.BedrockAlg;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Constructs an instance of a {@link BedrockAlg}
 *
 * @author DaPorkchop_
 */
public interface BedrockConstructor {
    BedrockAlg newInstance(@NonNull AtomicLong processed, @NonNull byte[] pattern, @NonNull Callback callback, int threads);
}
