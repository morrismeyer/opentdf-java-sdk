package io.opentdf.ffm;

import io.opentdf.ffm.internal.NativeBindings;
import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.PublicKeyResponseDto;
import io.opentdf.fory.dto.RewrapRequestDto;
import io.opentdf.fory.dto.RewrapResponseDto;

import java.lang.foreign.*;

/**
 * Client for the Key Access Service (KAS).
 * Handles key rewrap operations for TDF decryption.
 */
public class KASClient {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 1024 * 1024; // 1MB

    private final long handle;
    private final ForyCodec codec;
    private final Arena arena;

    KASClient(long handle, ForyCodec codec, Arena arena) {
        this.handle = handle;
        this.codec = codec;
        this.arena = arena;
    }

    /**
     * Algorithm constants for public key requests.
     */
    public enum Algorithm {
        RSA_2048(1),
        RSA_4096(2),
        EC_P256(3),
        EC_P384(4),
        EC_P521(5);

        private final int value;

        Algorithm(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Gets a KAS public key for the specified algorithm.
     *
     * @param algorithm the key algorithm
     * @return the public key response
     */
    public PublicKeyResponseDto getPublicKey(Algorithm algorithm) {
        int bufferSize = DEFAULT_BUFFER_SIZE;

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment keyBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.kasGetPublicKey.invokeExact(
                    handle, algorithm.getValue(), keyBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = keyBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return codec.deserialize(data, PublicKeyResponseDto.class);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to get KAS public key", t);
            }
        }

        throw new RuntimeException("Response too large");
    }

    /**
     * Performs a key rewrap operation.
     *
     * @param request the rewrap request
     * @return the rewrap response
     */
    public RewrapResponseDto rewrap(RewrapRequestDto request) {
        byte[] requestData = codec.serialize(request);
        int bufferSize = Math.max(DEFAULT_BUFFER_SIZE, requestData.length * 2);

        while (bufferSize <= MAX_BUFFER_SIZE) {
            try (Arena localArena = Arena.ofConfined()) {
                MemorySegment requestBuf = localArena.allocate(requestData.length);
                requestBuf.copyFrom(MemorySegment.ofArray(requestData));

                MemorySegment responseBuf = localArena.allocate(bufferSize);
                MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
                lenBuf.set(ValueLayout.JAVA_INT, 0, bufferSize);

                int result = (int) NativeBindings.kasRewrap.invokeExact(
                    handle,
                    requestBuf, requestData.length,
                    responseBuf, lenBuf);

                if (result == NativeBindings.OPENTDF_ERR_BUFFER_TOO_SMALL) {
                    bufferSize = lenBuf.get(ValueLayout.JAVA_INT, 0) * 2;
                    continue;
                }

                NativeBindings.checkResult(result);

                int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
                byte[] data = responseBuf.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
                return codec.deserialize(data, RewrapResponseDto.class);
            } catch (Throwable t) {
                throw new RuntimeException("Failed to perform rewrap", t);
            }
        }

        throw new RuntimeException("Response too large");
    }

    /**
     * Performs a rewrap operation with an unsigned request (for bulk operations).
     *
     * @param request the unsigned rewrap request
     * @return the rewrap response
     */
    public RewrapResponseDto rewrap(RewrapRequestDto.UnsignedRewrapRequestDto request) {
        // In a real implementation, this would sign the request first
        RewrapRequestDto signedRequest = new RewrapRequestDto("signed-token-placeholder");
        return rewrap(signedRequest);
    }
}
