package net.daporkchop.bedrock.util;

/**
 * Filters tiles so that only certain ones will be tested.
 *
 * @author DaPorkchop_
 */
public interface TileFilter {
    boolean test(int tileX, int tileZ);
}
