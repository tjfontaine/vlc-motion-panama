package org.videolan.vlc;
// Generated by jextract

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;

import static jdk.incubator.foreign.CSupport.SysV.*;

public class Clong {
    // don't create!
    Clong() {
    }

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(elemLayout)
                 .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }

    public static final MemoryLayout LAYOUT = C_LONG;
    public static final Class<?> CARRIER = long.class;
    private static final VarHandle handle = LAYOUT.varHandle(CARRIER);
    private static final VarHandle arrayHandle = arrayHandle(LAYOUT, CARRIER);

    public static MemoryAddress asArray(MemoryAddress addr, int numElements) {
        return MemorySegment.ofNativeRestricted(addr, numElements * LAYOUT.byteSize(),
               Thread.currentThread(), null, null).baseAddress();
    }

    public static long get(MemoryAddress addr) {
        return (long) handle.get(addr);
    }

    public static void set(MemoryAddress addr, long value) {
        handle.set(addr, value);
    }

    public static long get(MemoryAddress addr, long index) {
        return (long) arrayHandle.get(addr, index);
    }

    public static void set(MemoryAddress addr, long index, long value) {
        arrayHandle.set(addr, index, value);
    }

    public static MemorySegment allocate(long value) {
        var seg = MemorySegment.allocateNative(LAYOUT);
        handle.set(seg.baseAddress(), value);
        return seg;
    }

    public static MemoryAddress allocate(long value, NativeAllocationScope scope) {
        var addr = scope.allocate(LAYOUT);
        handle.set(addr, value);
        return addr;
    }

    public static MemorySegment allocateArray(int length) {
        var arrLayout = MemoryLayout.ofSequence(length, LAYOUT);
        return MemorySegment.allocateNative(arrLayout);
    }

    public static MemoryAddress allocateArray(int length, NativeAllocationScope scope) {
        var arrLayout = MemoryLayout.ofSequence(length, LAYOUT);
        return scope.allocate(arrLayout);
    }

    public static MemorySegment allocateArray(long[] arr) {
        var arrLayout = MemoryLayout.ofSequence(arr.length, LAYOUT);
        var seg = MemorySegment.allocateNative(arrLayout);
        seg.copyFrom(MemorySegment.ofArray(arr));
        return seg;
    }

    public static MemoryAddress allocateArray(long[] arr, NativeAllocationScope scope) {
        var arrLayout = MemoryLayout.ofSequence(arr.length, LAYOUT);
        var addr = scope.allocate(arrLayout);
        addr.segment().copyFrom(MemorySegment.ofArray(arr));
        return addr;
    }

    public static long sizeof() {
        return LAYOUT.byteSize();
    }

    public static long[] toJavaArray(MemorySegment seg) {
        var segSize = seg.byteSize();
        var elemSize = sizeof();
        if (segSize % elemSize != 0) {
            throw new UnsupportedOperationException("segment cannot contain integral number of elements");
        }
        long[] array = new long[(int) (segSize / elemSize)];
        MemorySegment.ofArray(array).copyFrom(seg);
        return array;
    }
}
