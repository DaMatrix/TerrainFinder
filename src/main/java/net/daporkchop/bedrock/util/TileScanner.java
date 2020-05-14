package net.daporkchop.bedrock.util;

/**
 * Scans tiles for certain features.
 *
 * @author DaPorkchop_
 */
public interface TileScanner {
    /**
     * Scans the given tile.
     *
     * @param tileX the tile's X coordinate
     * @param tileZ the tile's Z coordinate
     * @return a bitmask of all chunks that were found in the tile in XZ order
     */
    int scan(int tileX, int tileZ);
}
