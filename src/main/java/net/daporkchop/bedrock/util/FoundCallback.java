package net.daporkchop.bedrock.util;

/**
 * @author DaPorkchop_
 */
public interface FoundCallback {
    /**
     * Fired whenever a matching chunk is found.
     *
     * @param chunkX the chunk's X coordinate
     * @param chunkZ the chunk's Z coordinate
     * @return whether or not the search should continue
     */
    boolean found(int chunkX, int chunkZ);
}
