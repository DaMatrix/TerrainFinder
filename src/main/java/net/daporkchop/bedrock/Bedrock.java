package net.daporkchop.bedrock;

import net.daporkchop.bedrock.util.BedrockConstants;
import net.daporkchop.bedrock.util.RoughlySequentialLongSpliterator;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static net.daporkchop.bedrock.util.BedrockConstants.*;

/**
 * @author DaPorkchop_
 */
public class Bedrock {
    public static void main(String[] args) {
        //BedrockDialog.main();
        /*LongStream.range(0L, 32L << 2L)
                .filter(l -> (l & 3L) == 0L)
                .forEach(val -> System.out.printf("%d,%d\n", extractX(val), extractZ(val)));*/
        Set<Integer> positions = Collections.newSetFromMap(new ConcurrentHashMap<>());
        long result = StreamSupport.longStream(new RoughlySequentialLongSpliterator(0L, WHOLE_WORLD_CAP), true)
                .filter(l -> l == 100000000000L)
                .findAny().orElseThrow(IllegalStateException::new);
        System.out.println(result);
    }
}
