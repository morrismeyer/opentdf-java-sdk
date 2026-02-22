package io.opentdf.ffm;

import io.opentdf.ffm.internal.NativeBindings;
import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.EntityChainDto;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;

/**
 * Client for the Entity Resolution Service.
 * Resolves JWT tokens to entity chains.
 */
public class EntityResolver {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    private final long handle;
    private final ForyCodec codec;
    private final Arena arena;

    EntityResolver(long handle, ForyCodec codec, Arena arena) {
        this.handle = handle;
        this.codec = codec;
        this.arena = arena;
    }

    /**
     * Resolves a JWT token to an entity chain.
     *
     * @param token the JWT token to resolve
     * @return the resolved entity chain
     */
    public EntityChainDto resolve(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        byte[] tokenBytes = token.getBytes(StandardCharsets.UTF_8);
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment tokenBuf = localArena.allocate(tokenBytes.length);
                tokenBuf.copyFrom(MemorySegment.ofArray(tokenBytes));

                MemorySegment entityChainBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.entityResolve.invokeExact(
                    handle,
                    tokenBuf, tokenBytes.length,
                    entityChainBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = entityChainBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return codec.deserialize(data, EntityChainDto.class);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to resolve entity", t);
            }
        }

        throw new RuntimeException("Response too large");
    }
}
