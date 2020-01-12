package net.daporkchop.bedrock.mode.bedrock;

import net.daporkchop.bedrock.util.func.BedrockConstructor;

/**
 * All supported search modes for bedrock patterns
 *
 * @author DaPorkchop_
 */
public enum BedrockMode {
    FULL(16, Full::new, "Searches for a full 16x16 chunk pattern (fastest)"),
    SUB(8, Sub::new, "Searches for an 8x8 pattern in a single chunk (slower)"),
    ANY(8, Any::new, "Searches for an 8x8 pattern that may overlap into neighboring chunks (slowest)");

    public final int size;
    public final BedrockConstructor constructor;
    public final String desc;

    BedrockMode(int size, BedrockConstructor constructor, String desc) {
        this.size = size;
        this.constructor = constructor;
        this.desc = desc;
    }
}
