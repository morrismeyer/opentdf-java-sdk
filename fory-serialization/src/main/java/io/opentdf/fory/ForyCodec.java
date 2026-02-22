package io.opentdf.fory;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.Language;

/**
 * Codec for serializing and deserializing OpenTDF objects using Apache Fory.
 * Provides high-performance binary serialization for cross-language communication.
 *
 * Optimization techniques applied (from BUBBLE-OPTIMIZATION-TECHNIQUES.md):
 * - Section 0: Eliminate work - disabled unnecessary features
 * - Section 5: Zero-allocation - buffer reuse via ThreadSafeFory
 * - Section 9: Lookup tables - codegen for fast field access
 */
public class ForyCodec {
    private final ThreadSafeFory fory;

    /**
     * Creates a new ForyCodec with default configuration (JAVA mode, optimized).
     */
    public ForyCodec() {
        this(false);
    }

    /**
     * Creates a new ForyCodec with optional cross-language support.
     *
     * @param crossLanguage true to use XLANG mode for Go interop, false for JAVA mode
     */
    public ForyCodec(boolean crossLanguage) {
        ThreadSafeFory builtFory;

        if (crossLanguage) {
            // Cross-language mode for Go interop
            builtFory = Fory.builder()
                    .withLanguage(Language.XLANG)
                    .requireClassRegistration(true)
                    // Match Go Fory configuration
                    .withCompatibleMode(CompatibleMode.COMPATIBLE)
                    .withRefTracking(true)
                    // Disable JIT codegen to avoid conflicts with FFM native calls
                    .withCodegen(false)
                    .buildThreadSafeFory();
        } else {
            // Optimized JAVA mode for maximum performance
            builtFory = Fory.builder()
                    .withLanguage(Language.JAVA)
                    .requireClassRegistration(true)
                    // Optimization: SCHEMA_CONSISTENT for fixed schemas (no evolution overhead)
                    .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                    // Optimization: Enable codegen for specialized serializers
                    .withCodegen(true)
                    // Optimization: Disable reference tracking (no circular refs in DTOs)
                    .withRefTracking(false)
                    .buildThreadSafeFory();
        }

        this.fory = builtFory;
        ForyRegistry.registerTypes(this.fory);
    }

    /**
     * Creates a ForyCodec with a custom Fory instance.
     *
     * @param fory the pre-configured ThreadSafeFory instance
     */
    public ForyCodec(ThreadSafeFory fory) {
        this.fory = fory;
    }

    /**
     * Serializes an object to a byte array.
     *
     * @param obj the object to serialize
     * @return the serialized bytes
     */
    public byte[] serialize(Object obj) {
        return fory.serialize(obj);
    }

    /**
     * Deserializes a byte array to an object.
     *
     * @param data the serialized bytes
     * @param <T>  the expected type
     * @return the deserialized object
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data) {
        return (T) fory.deserialize(data);
    }

    /**
     * Deserializes a byte array to an object of the specified class.
     *
     * @param data  the serialized bytes
     * @param clazz the expected class
     * @param <T>   the expected type
     * @return the deserialized object
     */
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Object result = fory.deserialize(data);
        return clazz.cast(result);
    }

    /**
     * Returns the underlying Fory instance for advanced operations.
     *
     * @return the ThreadSafeFory instance
     */
    public ThreadSafeFory getFory() {
        return fory;
    }
}
