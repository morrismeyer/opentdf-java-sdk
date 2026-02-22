package io.opentdf.ffm;

import io.opentdf.ffm.internal.NativeBindings;
import io.opentdf.fory.ForyCodec;

import java.lang.foreign.*;
import java.nio.charset.StandardCharsets;

/**
 * Main OpenTDF Platform client that wraps the native library.
 * Provides high-level access to all platform services via FFM.
 */
public class OpenTDFPlatform implements AutoCloseable {
    private final long handle;
    private final ForyCodec codec;
    private final Arena arena;
    private volatile boolean closed = false;

    // Service clients (lazily initialized)
    private volatile KASClient kasClient;
    private volatile AuthorizationClient authorizationClient;
    private volatile PolicyClient policyClient;
    private volatile EntityResolver entityResolver;

    /**
     * Creates a new OpenTDFPlatform with the given configuration.
     *
     * @param configJson JSON configuration string
     */
    public OpenTDFPlatform(String configJson) {
        this.codec = new ForyCodec(true); // Cross-language mode for Go interop
        this.arena = Arena.ofShared();

        try {
            MemorySegment configSegment = arena.allocateFrom(configJson, StandardCharsets.UTF_8);
            this.handle = (long) NativeBindings.platformNew.invokeExact(configSegment);
            if (this.handle == 0) {
                throw new RuntimeException("Failed to initialize OpenTDF platform");
            }
        } catch (Throwable t) {
            arena.close();
            throw new RuntimeException("Failed to initialize OpenTDF platform", t);
        }
    }

    /**
     * Returns the KAS (Key Access Service) client.
     */
    public KASClient kas() {
        if (kasClient == null) {
            synchronized (this) {
                if (kasClient == null) {
                    kasClient = new KASClient(handle, codec, arena);
                }
            }
        }
        return kasClient;
    }

    /**
     * Returns the Authorization client.
     */
    public AuthorizationClient authorization() {
        if (authorizationClient == null) {
            synchronized (this) {
                if (authorizationClient == null) {
                    authorizationClient = new AuthorizationClient(handle, codec, arena);
                }
            }
        }
        return authorizationClient;
    }

    /**
     * Returns the Policy client.
     */
    public PolicyClient policy() {
        if (policyClient == null) {
            synchronized (this) {
                if (policyClient == null) {
                    policyClient = new PolicyClient(handle, codec, arena);
                }
            }
        }
        return policyClient;
    }

    /**
     * Returns the Entity Resolver.
     */
    public EntityResolver entityResolver() {
        if (entityResolver == null) {
            synchronized (this) {
                if (entityResolver == null) {
                    entityResolver = new EntityResolver(handle, codec, arena);
                }
            }
        }
        return entityResolver;
    }

    /**
     * Gets the platform version.
     */
    public String getVersion() {
        checkNotClosed();
        try (Arena localArena = Arena.ofConfined()) {
            MemorySegment versionBuf = localArena.allocate(64);
            MemorySegment lenBuf = localArena.allocate(ValueLayout.JAVA_INT);
            lenBuf.set(ValueLayout.JAVA_INT, 0, 64);

            int result = (int) NativeBindings.platformGetVersion.invokeExact(versionBuf, lenBuf);
            NativeBindings.checkResult(result);

            int len = lenBuf.get(ValueLayout.JAVA_INT, 0);
            // Copy bytes and create string - Go doesn't null-terminate
            byte[] bytes = new byte[len];
            MemorySegment.copy(versionBuf, ValueLayout.JAVA_BYTE, 0, bytes, 0, len);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to get platform version", t);
        }
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Platform has been closed");
        }
    }

    @Override
    public void close() {
        if (!closed) {
            synchronized (this) {
                if (!closed) {
                    closed = true;
                    try {
                        NativeBindings.platformFree.invokeExact(handle);
                    } catch (Throwable t) {
                        // Log but don't throw
                    } finally {
                        arena.close();
                    }
                }
            }
        }
    }
}
