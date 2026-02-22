package io.opentdf.ffm;

import io.opentdf.ffm.internal.NativeBindings;
import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.AttributeDto;
import io.opentdf.fory.dto.ValueDto;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Client for the Policy Service.
 * Handles policy attribute and value queries.
 */
public class PolicyClient {
    private static final int DEFAULT_BUFFER_SIZE = 16384;
    private static final int MAX_BUFFER_SIZE = 4 * 1024 * 1024;

    private final long handle;
    private final ForyCodec codec;
    private final Arena arena;

    PolicyClient(long handle, ForyCodec codec, Arena arena) {
        this.handle = handle;
        this.codec = codec;
        this.arena = arena;
    }

    /**
     * Lists attributes for the given namespace.
     *
     * @param namespace the namespace to list attributes for (null for all)
     * @return list of attributes
     */
    @SuppressWarnings("unchecked")
    public List<AttributeDto> listAttributes(String namespace) {
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment namespaceBuf = namespace != null
                    ? localArena.allocateFrom(namespace, StandardCharsets.UTF_8)
                    : MemorySegment.NULL;

                MemorySegment attributesBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.policyListAttributes.invokeExact(
                    handle, namespaceBuf,
                    attributesBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = attributesBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return (List<AttributeDto>) codec.deserialize(data);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to list attributes", t);
            }
        }

        throw new RuntimeException("Response too large");
    }

    /**
     * Gets attribute values for the given FQNs.
     *
     * @param fqns the fully qualified names to get values for
     * @return list of values
     */
    @SuppressWarnings("unchecked")
    public List<ValueDto> getAttributeValues(List<String> fqns) {
        byte[] fqnsData = codec.serialize(fqns);
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment fqnsBuf = localArena.allocate(fqnsData.length);
                fqnsBuf.copyFrom(MemorySegment.ofArray(fqnsData));

                MemorySegment valuesBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.policyGetAttributeValues.invokeExact(
                    handle,
                    fqnsBuf, fqnsData.length,
                    valuesBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = valuesBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return (List<ValueDto>) codec.deserialize(data);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get attribute values", t);
            }
        }

        throw new RuntimeException("Response too large");
    }
}
