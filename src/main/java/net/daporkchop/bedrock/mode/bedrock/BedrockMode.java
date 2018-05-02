package net.daporkchop.bedrock.mode.bedrock;

import net.daporkchop.bedrock.util.func.BedrockConstructor;

/**
 * All supported search modes for bedrock patterns
 *
 * @author DaPorkchop_
 */
public enum BedrockMode {
    FULL(16, Full::new,
            new byte[]{
                    1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0,
                    1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
                    1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0,
                    1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0,
                    0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0,
                    0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1,
                    0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1,
                    1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0,
                    0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1,
                    0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0,
                    0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0
            }),
    SUB(8, Sub::new,
            new byte[]{
                    0, 0, 1, 0, 0, 0, 1, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 1, 1, 0, 0, 0, 0,
                    0, 1, 1, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 1, 0, 1,
                    1, 0, 0, 0, 1, 0, 0, 0,
                    0, 0, 1, 0, 0, 1, 0, 1
            }),
    SUPER(8, Super::new,
            new byte[]{
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0, 0,
                    0, 1, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 0, 0, 0, 0,
                    0, 1, 1, 0, 0, 0, 0, 0,
                    0, 0, 0, 1, 1, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,
                    1, 0, 1, 0, 0, 1, 0, 0
            });

    public final int size;
    public final BedrockConstructor constructor;
    public final byte[] def;

    BedrockMode(int size, BedrockConstructor constructor, byte[] def) {
        this.size = size;
        this.constructor = constructor;
        this.def = def;
    }
}
