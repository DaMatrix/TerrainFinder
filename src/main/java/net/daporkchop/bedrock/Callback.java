package net.daporkchop.bedrock;

/**
 * Invoked after completion of a search
 *
 * @author DaPorkchop_
 */
@FunctionalInterface
public interface Callback {
    /**
     * The callback constructor
     *
     * @param x         the BLOCK x coordinate
     * @param z         the BLOCK z coordinate
     * @param processed the number of chunks searched
     * @return whether or not to abort the search
     */
    boolean onComplete(int x, int z, long processed);
}
