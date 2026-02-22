package io.opentdf.ffm;

import io.opentdf.ffm.internal.NativeBindings;
import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;

import java.lang.foreign.*;

/**
 * Client for the Authorization Service.
 * Handles authorization decisions and entitlement queries.
 */
public class AuthorizationClient {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    private final long handle;
    private final ForyCodec codec;
    private final Arena arena;

    AuthorizationClient(long handle, ForyCodec codec, Arena arena) {
        this.handle = handle;
        this.codec = codec;
        this.arena = arena;
    }

    /**
     * Gets an authorization decision for the given entity and resource.
     *
     * @param entityChain the entity chain to evaluate
     * @param resource    the resource to check access for
     * @return the decision response
     */
    public DecisionResponseDto getDecision(EntityChainDto entityChain, ResourceDto resource) {
        byte[] entityData = codec.serialize(entityChain);
        byte[] resourceData = codec.serialize(resource);
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment entityBuf = localArena.allocate(entityData.length);
                entityBuf.copyFrom(MemorySegment.ofArray(entityData));

                MemorySegment resourceBuf = localArena.allocate(resourceData.length);
                resourceBuf.copyFrom(MemorySegment.ofArray(resourceData));

                MemorySegment decisionBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.authGetDecision.invokeExact(
                    handle,
                    entityBuf, entityData.length,
                    resourceBuf, resourceData.length,
                    decisionBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = decisionBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return codec.deserialize(data, DecisionResponseDto.class);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get authorization decision", t);
            }
        }

        throw new RuntimeException("Response too large");
    }

    /**
     * Gets entitlements for the given entity.
     *
     * @param entity the entity to get entitlements for
     * @return the entitlements response
     */
    public EntitlementsResponseDto getEntitlements(EntityDto entity) {
        byte[] entityData = codec.serialize(entity);
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment entityBuf = localArena.allocate(entityData.length);
                entityBuf.copyFrom(MemorySegment.ofArray(entityData));

                MemorySegment entitlementsBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.authGetEntitlements.invokeExact(
                    handle,
                    entityBuf, entityData.length,
                    entitlementsBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = entitlementsBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return codec.deserialize(data, EntitlementsResponseDto.class);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get entitlements", t);
            }
        }

        throw new RuntimeException("Response too large");
    }
}
