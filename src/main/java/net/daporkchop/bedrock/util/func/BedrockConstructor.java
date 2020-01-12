package net.daporkchop.bedrock.util.func;

import lombok.NonNull;
import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.mode.bedrock.BedrockAlg;
import net.daporkchop.bedrock.util.RotationMode;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Constructs an instance of a {@link BedrockAlg}
 *
 * @author DaPorkchop_
 */
public interface BedrockConstructor {
    BedrockAlg newInstance(@NonNull byte[] pattern, @NonNull Callback callback, @NonNull RotationMode rotation, int threads);
}
