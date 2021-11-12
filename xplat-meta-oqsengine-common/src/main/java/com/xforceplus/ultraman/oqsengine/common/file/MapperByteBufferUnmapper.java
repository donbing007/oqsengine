package com.xforceplus.ultraman.oqsengine.common.file;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.filterReturnValue;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.StringTokenizer;

/**
 * 文件内存映射的释放工具.
 *
 * @author dongbin
 * @version 0.1 2021/11/11 16:33
 * @since 1.8
 */
public final class MapperByteBufferUnmapper {

    private static final Version VERSION = parseVersion(System.getProperty("java.specification.version"));

    private static final MethodHandle UNMAP;
    private static final RuntimeException UNMAP_NOT_SUPPORTED_EXCEPTION;

    static {
        Object unmap = null;
        RuntimeException exception = null;
        try {
            unmap = lookupUnmapMethodHandle();
        } catch (RuntimeException e) {
            exception = e;
        }
        if (unmap != null) {
            UNMAP = (MethodHandle) unmap;
            UNMAP_NOT_SUPPORTED_EXCEPTION = null;
        } else {
            UNMAP = null;
            UNMAP_NOT_SUPPORTED_EXCEPTION = exception;
        }
    }

    /**
     * 释放已经被map的内存映射实例.
     *
     * @param buffer 目标buffer.
     * @throws IOException 释放map时出现异常.
     */
    public static void unmap(MappedByteBuffer buffer) throws IOException {
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("Unmapping only works with direct buffers");
        }
        if (UNMAP == null) {
            throw UNMAP_NOT_SUPPORTED_EXCEPTION;
        }

        try {
            UNMAP.invokeExact((ByteBuffer) buffer);
        } catch (Throwable throwable) {
            throw new IOException("Unable to unmap the mapped buffer.", throwable);
        }
    }


    private static MethodHandle lookupUnmapMethodHandle() {
        final MethodHandles.Lookup lookup = lookup();
        try {
            if (VERSION.isJava9Compatible()) {
                return unmapJava9(lookup);
            } else {
                return unmapJava7Or8(lookup);
            }
        } catch (ReflectiveOperationException | RuntimeException e1) {
            throw new UnsupportedOperationException(
                "Unmapping is not supported on this platform, because internal Java APIs are not compatible.", e1);
        }
    }

    private static MethodHandle unmapJava7Or8(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        /*
         * 最终执行如下逻辑.
         *
         * (ByteBuffer buffer) -> {
         *   sun.misc.Cleaner cleaner = ((java.nio.DirectByteBuffer) byteBuffer).cleaner();
         *   if (nonNull(cleaner))
         *     cleaner.clean();
         *   else
         *     noop(cleaner); // the noop is needed because MethodHandles#guardWithTest always needs both if and else
         * }
         */
        Class<?> directBufferClass = Class.forName("java.nio.DirectByteBuffer");
        Method m = directBufferClass.getMethod("cleaner");
        m.setAccessible(true);
        MethodHandle directBufferCleanerMethod = lookup.unreflect(m);
        Class<?> cleanerClass = directBufferCleanerMethod.type().returnType();
        MethodHandle cleanMethod = lookup.findVirtual(cleanerClass, "clean", methodType(void.class));
        MethodHandle nonNullTest = lookup.findStatic(MapperByteBufferUnmapper.class, "nonNull",
            methodType(boolean.class, Object.class)).asType(methodType(boolean.class, cleanerClass));
        MethodHandle noop = dropArguments(constant(Void.class, null).asType(methodType(void.class)), 0, cleanerClass);
        MethodHandle unmapper =
            filterReturnValue(directBufferCleanerMethod, guardWithTest(nonNullTest, cleanMethod, noop))
                .asType(methodType(void.class, ByteBuffer.class));
        return unmapper;
    }

    private static MethodHandle unmapJava9(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        MethodHandle unmapper = lookup.findVirtual(unsafeClass, "invokeCleaner",
            methodType(void.class, ByteBuffer.class));
        Field f = unsafeClass.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        Object theUnsafe = f.get(null);
        return unmapper.bindTo(theUnsafe);
    }

    private static boolean nonNull(Object o) {
        return o != null;
    }

    private static Version parseVersion(String versionString) {
        final StringTokenizer st = new StringTokenizer(versionString, ".");
        int majorVersion = Integer.parseInt(st.nextToken());
        int minorVersion;
        if (st.hasMoreTokens()) {
            minorVersion = Integer.parseInt(st.nextToken());
        } else {
            minorVersion = 0;
        }
        return new Version(majorVersion, minorVersion);
    }

    static class Version {
        public final int majorVersion;
        public final int minorVersion;

        private Version(int majorVersion, int minorVersion) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }

        @Override
        public String toString() {
            return "Version(majorVersion=" + majorVersion + ", minorVersion=" + minorVersion + ")";
        }

        public boolean isJava9Compatible() {
            return majorVersion >= 9;
        }

        public boolean isJava11Compatible() {
            return majorVersion >= 11;
        }

    }
}
