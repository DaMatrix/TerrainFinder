package net.daporkchop.bedrock.gpu.bedrock;

import net.daporkchop.bedrock.Callback;
import net.daporkchop.bedrock.mode.Modes;
import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_MEM_COPY_HOST_PTR;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clSetKernelArg;

/**
 * @author DaPorkchop_
 */
public class BedrockGPU {
    private final int[] srcX, srcZ, dst, pattern;
    private final cl_mem[] memObjects = new cl_mem[4];
    private final cl_kernel kernel;
    private final cl_command_queue commandQueue;
    private final long[] global_work_size, local_work_size = new long[]{1};
    private final Pointer dstPointer;
    private final int batchSize;

    public BedrockGPU(Modes mode, int batchSize, byte[] patternIn) {
        this.batchSize = batchSize;
        global_work_size = new long[]{batchSize};
        srcX = new int[batchSize];
        srcZ = new int[batchSize];
        dst = new int[batchSize];
        pattern = new int[patternIn.length];
        for (int i = 0; i < patternIn.length; i++) {
            pattern[i] = patternIn[i] & 0xFF;
        }

        Pointer srcA = Pointer.to(srcX);
        Pointer srcB = Pointer.to(srcZ);
        dstPointer = Pointer.to(this.dst);
        Pointer pattern = Pointer.to(this.pattern);

        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        cl_context context = clCreateContext(contextProperties, 1, new cl_device_id[]{device}, null, null, null);

        commandQueue = clCreateCommandQueue(context, device, 0, null);

        memObjects[0] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * batchSize, srcA, null);
        memObjects[1] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * batchSize, srcB, null);
        memObjects[2] = clCreateBuffer(context, CL_MEM_READ_WRITE, Sizeof.cl_int * batchSize, null, null);
        memObjects[3] = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * batchSize, pattern, null);

        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{new Scanner(getClass().getResourceAsStream("/bedrock_full.cl"), "UTF-8").useDelimiter("\\A").next()}, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, "sampleKernel", null);

        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(memObjects[2]));
        clSetKernelArg(kernel, 3, Sizeof.cl_mem, Pointer.to(memObjects[3]));
    }

    public static void main(String... args) {
        BedrockGPU gpu = new BedrockGPU(null, 4, Modes.FULL.def);
        gpu.scan(0, 1, 0, 1875000, (x, z) -> System.out.println("done! " + x + " " + z), new AtomicBoolean(true));
    }

    public void scan(int id, int step, int start, int end, Callback callback, AtomicBoolean running) {

    }
}
