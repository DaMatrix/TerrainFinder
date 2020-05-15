package net.daporkchop.bedrock.mode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.daporkchop.bedrock.util.RotationMode;
import net.daporkchop.bedrock.util.TileScanner;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum SearchMode {
    FULL(16, "Searches for a full, chunk-aligned 16² pattern") {
        @Override
        public TileScanner create(@NonNull byte[] pattern, @NonNull RotationMode rotation) {
            return new BedrockFullScanner(pattern, rotation);
        }
    };

    private final int size;

    @NonNull
    private final String description;

    public abstract TileScanner create(@NonNull byte[] pattern, @NonNull RotationMode rotation);
}
