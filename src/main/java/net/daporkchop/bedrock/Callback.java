package net.daporkchop.bedrock;

/**
 * Invoked after completion of a search
 *
 * @author DaPorkchop_
 */
public interface Callback {
    /**
     * The callback function
     *
     * @param x         the BLOCK x coordinate
     * @param z         the BLOCK z coordinate
     * @param processed the number of chunks searched
     */
    void onComplete(int x, int z, long processed);
}
