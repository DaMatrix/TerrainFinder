package net.daporkchop.bedrock.mode;

/**
 * @author DaPorkchop_
 */
public interface ISearchMode {
    void run(byte[] pattern, int id, int step, int start, int end);
}
