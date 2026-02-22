package io.opentdf.benchmarks;

import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks focused on measuring GC pressure and allocation rates.
 * Run with -prof gc to get allocation metrics.
 *
 * Key metrics to watch:
 * - gc.alloc.rate: Allocation rate (MB/sec)
 * - gc.alloc.rate.norm: Bytes allocated per operation
 * - gc.count: GC count during benchmark
 * - gc.time: Total GC time
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = {"-Xmx512m", "-XX:+UseG1GC", "-XX:+AlwaysPreTouch"})
public class GcPressureBenchmark {

    private ForyCodec foryCodec;

    private PolicyBindingDto smallObject;
    private KeyAccessDto mediumObject;
    private RewrapRequestDto.UnsignedRewrapRequestDto largeObject;

    private byte[] serializedSmall;
    private byte[] serializedMedium;
    private byte[] serializedLarge;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Small object (few fields)
        smallObject = new PolicyBindingDto("HS256", "hashvalue123");

        // Medium object (nested structures)
        mediumObject = new KeyAccessDto();
        mediumObject.setKeyType("wrapped");
        mediumObject.setKasUrl("https://kas.example.com");
        mediumObject.setKid("key-123");
        mediumObject.setProtocol("kas");
        mediumObject.setWrappedKey(new byte[256]);
        mediumObject.setPolicyBinding(smallObject);

        // Large object (complex nested structures)
        largeObject = createLargeRequest(10);

        // Pre-serialize
        serializedSmall = foryCodec.serialize(smallObject);
        serializedMedium = foryCodec.serialize(mediumObject);
        serializedLarge = foryCodec.serialize(largeObject);
    }

    private RewrapRequestDto.UnsignedRewrapRequestDto createLargeRequest(int numPolicies) {
        RewrapRequestDto.UnsignedRewrapRequestDto request = new RewrapRequestDto.UnsignedRewrapRequestDto();
        request.setClientPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...\n-----END PUBLIC KEY-----");

        List<RewrapRequestDto.WithPolicyRequestDto> policyRequests = new ArrayList<>();
        for (int i = 0; i < numPolicies; i++) {
            RewrapRequestDto.WithPolicyRequestDto policyRequest = new RewrapRequestDto.WithPolicyRequestDto();

            KeyAccessDto keyAccess = new KeyAccessDto();
            keyAccess.setKeyType("wrapped");
            keyAccess.setKasUrl("https://kas.example.com");
            keyAccess.setKid("key-" + i);
            keyAccess.setProtocol("kas");
            keyAccess.setWrappedKey(new byte[256]);
            keyAccess.setPolicyBinding(new PolicyBindingDto("HS256", "hash-" + i));

            RewrapRequestDto.WithKeyAccessObjectDto kao = new RewrapRequestDto.WithKeyAccessObjectDto();
            kao.setKeyAccessObjectId("kao-" + i);
            kao.setKeyAccessObject(keyAccess);

            RewrapRequestDto.WithPolicyDto policy = new RewrapRequestDto.WithPolicyDto();
            policy.setId("policy-" + i);
            policy.setBody("eyJhdHRyaWJ1dGVzIjpbXX0=");

            policyRequest.setKeyAccessObjects(List.of(kao));
            policyRequest.setPolicy(policy);
            policyRequest.setAlgorithm("rsa:2048");

            policyRequests.add(policyRequest);
        }
        request.setRequests(policyRequests);
        return request;
    }

    // ============== Small Object Tests ==============

    @Benchmark
    public byte[] serializeSmall() {
        return foryCodec.serialize(smallObject);
    }

    @Benchmark
    public void deserializeSmall(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedSmall, PolicyBindingDto.class));
    }

    // ============== Medium Object Tests ==============

    @Benchmark
    public byte[] serializeMedium() {
        return foryCodec.serialize(mediumObject);
    }

    @Benchmark
    public void deserializeMedium(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedMedium, KeyAccessDto.class));
    }

    // ============== Large Object Tests ==============

    @Benchmark
    public byte[] serializeLarge() {
        return foryCodec.serialize(largeObject);
    }

    @Benchmark
    public void deserializeLarge(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedLarge, RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    // ============== Burst Allocation Tests ==============

    /**
     * Simulates a burst of serialization operations.
     * Useful for measuring GC impact under load.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public void burstSerialize(Blackhole bh) {
        for (int i = 0; i < 100; i++) {
            bh.consume(foryCodec.serialize(mediumObject));
        }
    }

    /**
     * Simulates a burst of deserialization operations.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public void burstDeserialize(Blackhole bh) {
        for (int i = 0; i < 100; i++) {
            bh.consume(foryCodec.deserialize(serializedMedium, KeyAccessDto.class));
        }
    }

    /**
     * Mixed workload: serialize and deserialize in bursts.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public void burstMixed(Blackhole bh) {
        for (int i = 0; i < 50; i++) {
            byte[] data = foryCodec.serialize(mediumObject);
            bh.consume(foryCodec.deserialize(data, KeyAccessDto.class));
        }
    }
}
