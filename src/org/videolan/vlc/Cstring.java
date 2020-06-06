package org.videolan.vlc;
// Generated by jextract

import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import jdk.incubator.foreign.NativeAllocationScope;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static jdk.incubator.foreign.CSupport.C_CHAR;

public final class Cstring {
    // don't create!
    private Cstring() {
    }

    private static VarHandle arrayHandle(MemoryLayout elemLayout, Class<?> elemCarrier) {
        return MemoryLayout.ofSequence(elemLayout)
                .varHandle(elemCarrier, MemoryLayout.PathElement.sequenceElement());
    }
    private final static VarHandle byteArrHandle = arrayHandle(C_CHAR, byte.class);

    private static MemorySegment toCString(byte[] bytes) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(bytes.length + 1, C_CHAR);
        MemorySegment segment = MemorySegment.allocateNative(strLayout);
        MemoryAddress addr = segment.baseAddress();
        copy(addr, bytes);
        return segment;
    }

    private static MemoryAddress toCString(byte[] bytes, NativeAllocationScope scope) {
        MemoryLayout strLayout = MemoryLayout.ofSequence(bytes.length + 1, C_CHAR);
        MemoryAddress addr = scope.allocate(strLayout);
        addr.segment().copyFrom(MemorySegment.ofArray(bytes));
        return addr;
    }

    public static void copy(MemoryAddress addr, String str) {
        copy(addr, str.getBytes());
    }

    public static void copy(MemoryAddress addr, String str, Charset charset) {
        copy(addr, str.getBytes(charset));
    }

    //where
    private static void copy(MemoryAddress addr, byte[] bytes) {
            var heapSegment = MemorySegment.ofArray(bytes);
            addr.segment()
                    .asSlice(addr.segmentOffset(), bytes.length)
                    .copyFrom(heapSegment);
            byteArrHandle.set(addr, (long)bytes.length, (byte)0);
        }

    public static MemorySegment toCString(String str) {
         return toCString(str.getBytes());
    }

    public static MemorySegment toCString(String str, Charset charset) {
         return toCString(str.getBytes(charset));
    }

    public static MemoryAddress toCString(String str, NativeAllocationScope scope) {
        return toCString(str.getBytes(), scope);
    }

    public static MemoryAddress toCString(String str, Charset charset, NativeAllocationScope scope) {
        return toCString(str.getBytes(charset), scope);
    }

    public static String toJavaString(MemoryAddress addr) {
        StringBuilder buf = new StringBuilder();
        MemoryAddress baseAddr = addr.segment() != null ?
                addr :
                MemorySegment.ofNativeRestricted(addr, Long.MAX_VALUE, Thread.currentThread(),
                        null, null).baseAddress();
        byte curr = (byte) byteArrHandle.get(baseAddr, 0);
        long offset = 0;
        while (curr != 0) {
            buf.append((char) curr);
            curr = (byte) byteArrHandle.get(baseAddr, ++offset);
        }
        return buf.toString();
    }
}
