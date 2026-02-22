package io.opentdf.ffm.internal;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;

/**
 * Low-level FFM bindings to the libopentdf native library.
 * Uses Java's Foreign Function & Memory (FFM) API for native interop.
 */
public final class NativeBindings {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup SYMBOLS;

    // Error codes
    public static final int OPENTDF_OK = 0;
    public static final int OPENTDF_ERR_INVALID_HANDLE = 1;
    public static final int OPENTDF_ERR_INVALID_INPUT = 2;
    public static final int OPENTDF_ERR_SERIALIZATION = 3;
    public static final int OPENTDF_ERR_INTERNAL = 4;
    public static final int OPENTDF_ERR_NOT_FOUND = 5;
    public static final int OPENTDF_ERR_UNAUTHORIZED = 6;
    public static final int OPENTDF_ERR_BUFFER_TOO_SMALL = 7;

    // Platform lifecycle
    public static final MethodHandle platformNew;
    public static final MethodHandle platformFree;
    public static final MethodHandle platformGetVersion;

    // KAS
    public static final MethodHandle kasGetPublicKey;
    public static final MethodHandle kasRewrap;

    // Authorization
    public static final MethodHandle authGetDecision;
    public static final MethodHandle authGetEntitlements;

    // Policy
    public static final MethodHandle policyListAttributes;
    public static final MethodHandle policyGetAttributeValues;

    // Entity Resolution
    public static final MethodHandle entityResolve;

    static {
        SYMBOLS = loadLibrary();

        // Platform lifecycle
        platformNew = downcall("PlatformNew",
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        platformFree = downcall("PlatformFree",
            FunctionDescriptor.ofVoid(ValueLayout.JAVA_LONG));
        platformGetVersion = downcall("PlatformGetVersion",
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // KAS
        kasGetPublicKey = downcall("KASGetPublicKey",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        kasRewrap = downcall("KASRewrap",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // Authorization
        authGetDecision = downcall("AuthGetDecision",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        authGetEntitlements = downcall("AuthGetEntitlements",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // Policy
        policyListAttributes = downcall("PolicyListAttributes",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG, ValueLayout.ADDRESS,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        policyGetAttributeValues = downcall("PolicyGetAttributeValues",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        // Entity Resolution
        entityResolve = downcall("EntityResolve",
            FunctionDescriptor.of(ValueLayout.JAVA_INT,
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS, ValueLayout.ADDRESS));
    }

    private NativeBindings() {
        // Utility class
    }

    private static SymbolLookup loadLibrary() {
        // Try to load from various locations
        String libName = System.mapLibraryName("opentdf");

        // Check for library path environment variable
        String libPath = System.getenv("OPENTDF_LIB_PATH");
        if (libPath != null) {
            try {
                return SymbolLookup.libraryLookup(Path.of(libPath, libName), Arena.global());
            } catch (Exception e) {
                // Fall through to other options
            }
        }

        // Check current directory
        try {
            return SymbolLookup.libraryLookup(Path.of(libName), Arena.global());
        } catch (Exception e) {
            // Fall through
        }

        // Check lib subdirectory
        try {
            return SymbolLookup.libraryLookup(Path.of("lib", libName), Arena.global());
        } catch (Exception e) {
            // Fall through
        }

        // Check relative to class location (for native-image)
        try {
            String classPath = NativeBindings.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            Path libDir = Path.of(classPath).getParent();
            return SymbolLookup.libraryLookup(libDir.resolve(libName), Arena.global());
        } catch (Exception e) {
            // Fall through
        }

        // Check standard library paths
        try {
            return SymbolLookup.libraryLookup(libName, Arena.global());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load libopentdf native library. " +
                "Set OPENTDF_LIB_PATH environment variable or place " + libName +
                " in the current directory.", e);
        }
    }

    private static MethodHandle downcall(String name, FunctionDescriptor descriptor) {
        MemorySegment symbol = SYMBOLS.find(name)
            .orElseThrow(() -> new RuntimeException("Symbol not found: " + name));
        return LINKER.downcallHandle(symbol, descriptor);
    }

    /**
     * Checks the result code and throws an exception if it indicates an error.
     */
    public static void checkResult(int result) {
        if (result != OPENTDF_OK) {
            throw new NativeException(result);
        }
    }

    /**
     * Exception thrown when a native call fails.
     */
    public static class NativeException extends RuntimeException {
        private final int errorCode;

        public NativeException(int errorCode) {
            super(getErrorMessage(errorCode));
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }

        private static String getErrorMessage(int code) {
            return switch (code) {
                case OPENTDF_ERR_INVALID_HANDLE -> "Invalid handle";
                case OPENTDF_ERR_INVALID_INPUT -> "Invalid input";
                case OPENTDF_ERR_SERIALIZATION -> "Serialization error";
                case OPENTDF_ERR_INTERNAL -> "Internal error";
                case OPENTDF_ERR_NOT_FOUND -> "Not found";
                case OPENTDF_ERR_UNAUTHORIZED -> "Unauthorized";
                case OPENTDF_ERR_BUFFER_TOO_SMALL -> "Buffer too small";
                default -> "Unknown error: " + code;
            };
        }
    }
}
