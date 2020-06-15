package org.videolan.vlc;
// Generated by jextract

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeScope;

import static jdk.incubator.foreign.CSupport.SysV.*;

public class Cfloat {
    // don't create!
    Cfloat() {
    }

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(elemLayout)
                 .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }

    public static final MemoryLayout LAYOUT = C_FLOAT;
    public static final Class<?> CARRIER = float.class;
    private static final VarHandle handle = LAYOUT.varHandle(CARRIER);
    private static final VarHandle arrayHandle = arrayHandle(LAYOUT, CARRIER);

    public static MemoryAddress asArrayRestricted(MemoryAddress addr, int numElements) {
        return MemorySegment.ofNativeRestricted(addr, numElements * LAYOUT.byteSize(),
               Thread.currentThread(), null, null).baseAddress();
    }

    public static MemoryAddress asArray(MemoryAddress addr, int numElements) {
        var seg = addr.segment();
        if (seg == null) {
            throw new IllegalArgumentException("no underlying segment for the address");
        }
        return seg.asSlice(addr.segmentOffset(), numElements * LAYOUT.byteSize()).baseAddress();
    }

    public static float get(MemoryAddress addr) {
        return (float) handle.get(addr);
    }

    public static void set(MemoryAddress addr, float value) {
        handle.set(addr, value);
    }

    public static float get(MemoryAddress addr, long index) {
        return (float) arrayHandle.get(addr, index);
    }

    public static void set(MemoryAddress addr, long index, float value) {
        arrayHandle.set(addr, index, value);
    }

    public static MemorySegment allocate(float value) {
        var seg = MemorySegment.allocateNative(LAYOUT);
        handle.set(seg.baseAddress(), value);
        return seg;
    }

    public static MemoryAddress allocate(float value, NativeScope scope) {
        var addr = scope.allocate(LAYOUT);
        handle.set(addr, value);
        return addr;
    }

    public static MemorySegment allocateArray(int length) {
        var arrLayout = MemoryLayout.ofSequence(length, LAYOUT);
        return MemorySegment.allocateNative(arrLayout);
    }

    public static MemoryAddress allocateArray(int length, NativeScope scope) {
        var arrLayout = MemoryLayout.ofSequence(length, LAYOUT);
        return scope.allocate(arrLayout);
    }

    public static MemorySegment allocateArray(float[] arr) {
        var arrLayout = MemoryLayout.ofSequence(arr.length, LAYOUT);
        var seg = MemorySegment.allocateNative(arrLayout);
        seg.copyFrom(MemorySegment.ofArray(arr));
        return seg;
    }

    public static MemoryAddress allocateArray(float[] arr, NativeScope scope) {
        var arrLayout = MemoryLayout.ofSequence(arr.length, LAYOUT);
        var addr = scope.allocate(arrLayout);
        addr.segment().copyFrom(MemorySegment.ofArray(arr));
        return addr;
    }

    public static long sizeof() {
        return LAYOUT.byteSize();
    }

    public static float[] toJavaArray(MemorySegment seg) {
        var segSize = seg.byteSize();
        var elemSize = sizeof();
        if (segSize % elemSize != 0) {
            throw new UnsupportedOperationException("segment cannot contain integral number of elements");
        }
        float[] array = new float[(int) (segSize / elemSize)];
        MemorySegment.ofArray(array).copyFrom(seg);
        return array;
    }
}
