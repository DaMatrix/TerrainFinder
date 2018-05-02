package net.daporkchop.bedrock.gpu.bedrock;

import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.mode.Modes;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static jdk.nashorn.internal.objects.Global.print;
import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_CPU;
import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.CL_PROGRAM_BUILD_LOG;
import static org.lwjgl.opencl.CL10.CL_QUEUE_PROFILING_ENABLE;
import static org.lwjgl.opencl.CL10.clBuildProgram;
import static org.lwjgl.opencl.CL10.clCreateBuffer;
import static org.lwjgl.opencl.CL10.clCreateCommandQueue;
import static org.lwjgl.opencl.CL10.clCreateKernel;
import static org.lwjgl.opencl.CL10.clCreateProgramWithSource;
import static org.lwjgl.opencl.CL10.clEnqueueNDRangeKernel;
import static org.lwjgl.opencl.CL10.clEnqueueWriteBuffer;
import static org.lwjgl.opencl.CL10.clFinish;
import static org.lwjgl.opencl.CL10.clGetProgramBuildInfo;

/**
 * @author DaPorkchop_
 */
public class BedrockGPU {
    private final int batchSize;
    private final ByteBuffer pattern_buffer;
    private CLKernel kernel;
    private CLMem patternMem;
    private CLCommandQueue queue;

    public BedrockGPU(Modes mode, int batchSize, int[] full_pattern) {
        this.batchSize = batchSize;
        pattern_buffer = intToByteBuffer(full_pattern);

        try {
            CL.create();
            CLPlatform platform = CLPlatform.getPlatforms().get(0);
            List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_CPU);
            CLContextCallback contextCB = new CLContextCallback() {
                @Override
                protected void handleMessage(String s, ByteBuffer byteBuffer) {
                    System.err.println("cl_context_callback info: " + s);
                }
            };
            CLContext context = CLContext.create(platform, devices, contextCB, null, null);
            queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);

            patternMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, pattern_buffer, null);
            clEnqueueWriteBuffer(queue, patternMem, 1, 0, pattern_buffer, null, null);
            final String bedrock_source = new Scanner(getClass().getResourceAsStream("/bedrock_full.cl"), "UTF-8").useDelimiter("\\A").next();
            CLProgram program = clCreateProgramWithSource(context, bedrock_source, null);
            int err = clBuildProgram(program, devices.get(0), "", null);
            if (err != 0) {
                printBuildLog(program, devices.get(0));
                Util.checkCLError(err);
            }
            kernel = clCreateKernel(program, "sampleKernel", null);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String... args) {
        int[] pattern = new int[16 * 16];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Modes.FULL.def[i] & 0xFF;
        }
        BedrockGPU gpu = new BedrockGPU(null, 4, pattern);
        gpu.scan(0, 16, 0, 1875000, (x, z) -> System.out.println("done! " + x + " " + z), new AtomicBoolean(true));
    }

    private static ByteBuffer intToByteBuffer(int[] ints) {
        ByteBuffer buffer = BufferUtils.createByteBuffer(ints.length);
        for (int i = 0; i < ints.length; i++) {
            buffer.put(i, (byte) ints[i]);
        }
        return buffer;
    }

    private static void printBuildLog(CLProgram program, CLDevice device) {
        PointerBuffer size = PointerBuffer.allocateDirect(1); // can only get value once or jvm will crash for some reason
        clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, null, size); // get the length
        ByteBuffer buffer = BufferUtils.createByteBuffer((int) size.get());
        clGetProgramBuildInfo(program, device, CL_PROGRAM_BUILD_LOG, buffer, null); // write to buffer
        print(buffer);
    }

    public void scan(int id, int step, int start, int end, Callback callback, AtomicBoolean running) {
        for (int r = start + id; r < end; r += step) {
            for (int i = -r; i <= r; i++) {
                run(i, r);
                run(i, -r);
            }
        }
    }

    private void run(int x, int z) {
        PointerBuffer bedrockWorkSize = BufferUtils.createPointerBuffer(1);
        bedrockWorkSize.put(0, 1);

        kernel.setArg(0, patternMem); // pattern
        kernel.setArg(1, x); // start
        kernel.setArg(2, z); // end
        // execute kernel
        clEnqueueNDRangeKernel(queue, kernel, 1, null, bedrockWorkSize, null, null, null);
        clFinish(queue);
    }
}
