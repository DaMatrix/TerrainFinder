package net.daporkchop.bedrock.util;

import lombok.experimental.UtilityClass;
import net.daporkchop.lib.common.math.BinMath;

import static java.lang.Math.*;
import static net.daporkchop.lib.common.math.PMath.*;

/**
 * @author DaPorkchop_
 */
@UtilityClass
public class BedrockConstants {
    public static final int TILE_SIZE = 16; //the size of a tile in chunks
    public static final int TILE_BITS = 4;

    public static final int WORLD_RADIUS = 30000000 >> 4 >> TILE_BITS; //the radius of the world in tiles
    public static final long WHOLE_WORLD_CAP = ((long) WORLD_RADIUS * WORLD_RADIUS) << 2L;

    public static int extractX(long l) {
        //i'll be able to understand how this works ever again, but that doesn't matter because it works now
        //...right?
        long base = floorL(sqrt(l >> 2L));
        long offset = (l >> 2L) - base * base;
        long val = min(offset, base);
        return (int) (val ^ (l & 1L));
    }

    public static int extractZ(long l) {
        long base = floorL(sqrt(l >> 2L));
        long offset = (l >> 2L) - base * base;
        long val = offset >= base + 1L ? base - (offset >> 1L) : base;
        return (int) (val ^ ((l >> 1L) & 1L));
    }
}
