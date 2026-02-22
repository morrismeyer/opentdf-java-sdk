package io.opentdf.benchmarks;

import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmarks comparing Fory serialization performance with Protobuf.
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, jvmArgs = {"-Xmx2g", "-XX:+UseG1GC"})
public class SerializationBenchmark {

    private ForyCodec foryCodec;

    // Sample DTOs for benchmarking
    private PolicyBindingDto policyBinding;
    private KeyAccessDto keyAccess;
    private EntityChainDto entityChain;
    private RewrapRequestDto.UnsignedRewrapRequestDto rewrapRequest;
    private DecisionResponseDto decisionResponse;

    // Pre-serialized data for deserialization benchmarks
    private byte[] serializedPolicyBinding;
    private byte[] serializedKeyAccess;
    private byte[] serializedEntityChain;
    private byte[] serializedRewrapRequest;
    private byte[] serializedDecisionResponse;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Create sample data
        policyBinding = new PolicyBindingDto("HS256", "abc123hashvalue456def");

        keyAccess = new KeyAccessDto();
        keyAccess.setKeyType("wrapped");
        keyAccess.setKasUrl("https://kas.example.com/api/v1");
        keyAccess.setKid("key-identifier-12345");
        keyAccess.setProtocol("kas");
        keyAccess.setWrappedKey(new byte[256]); // Typical wrapped key size
        keyAccess.setPolicyBinding(policyBinding);
        keyAccess.setEncryptedMetadata("base64encodedmetadata==");

        EntityDto entity1 = new EntityDto("e1", EntityDto.EntityType.EMAIL_ADDRESS,
            "bob@example.com", EntityDto.Category.SUBJECT);
        EntityDto entity2 = new EntityDto("e2", EntityDto.EntityType.CLIENT_ID,
            "client-application-123", EntityDto.Category.ENVIRONMENT);
        // Use array constructor for optimized serialization (Section 12a: Contiguous Storage)
        entityChain = new EntityChainDto("chain-1", new EntityDto[]{entity1, entity2});

        rewrapRequest = new RewrapRequestDto.UnsignedRewrapRequestDto();
        rewrapRequest.setClientPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----");

        RewrapRequestDto.WithKeyAccessObjectDto kao = new RewrapRequestDto.WithKeyAccessObjectDto();
        kao.setKeyAccessObjectId("kao-1");
        kao.setKeyAccessObject(keyAccess);

        RewrapRequestDto.WithPolicyDto policy = new RewrapRequestDto.WithPolicyDto("policy-1",
            "eyJhdHRyaWJ1dGVzIjpbeyJmcW4iOiJodHRwczovL2V4YW1wbGUuY29tL2F0dHIvY2xhc3NpZmljYXRpb24vdmFsdWUvc2VjcmV0In1dfQ==");

        RewrapRequestDto.WithPolicyRequestDto policyRequest = new RewrapRequestDto.WithPolicyRequestDto();
        policyRequest.setKeyAccessObjects(Arrays.asList(kao));
        policyRequest.setPolicy(policy);
        policyRequest.setAlgorithm("rsa:2048");
        rewrapRequest.setRequests(Arrays.asList(policyRequest));

        decisionResponse = new DecisionResponseDto();
        decisionResponse.setEntityChainId("ec1");
        decisionResponse.setResourceAttributesId("attr-set-1");
        decisionResponse.setDecision(DecisionResponseDto.Decision.PERMIT);
        decisionResponse.setObligations(Arrays.asList(
            "http://example.org/obligation/watermark",
            "http://example.org/obligation/audit"
        ));
        decisionResponse.setAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));

        // Pre-serialize for deserialization benchmarks
        serializedPolicyBinding = foryCodec.serialize(policyBinding);
        serializedKeyAccess = foryCodec.serialize(keyAccess);
        serializedEntityChain = foryCodec.serialize(entityChain);
        serializedRewrapRequest = foryCodec.serialize(rewrapRequest);
        serializedDecisionResponse = foryCodec.serialize(decisionResponse);
    }

    // ============== Serialization Benchmarks ==============

    @Benchmark
    public byte[] forySerializePolicyBinding() {
        return foryCodec.serialize(policyBinding);
    }

    @Benchmark
    public byte[] forySerializeKeyAccess() {
        return foryCodec.serialize(keyAccess);
    }

    @Benchmark
    public byte[] forySerializeEntityChain() {
        return foryCodec.serialize(entityChain);
    }

    @Benchmark
    public byte[] forySerializeRewrapRequest() {
        return foryCodec.serialize(rewrapRequest);
    }

    @Benchmark
    public byte[] forySerializeDecisionResponse() {
        return foryCodec.serialize(decisionResponse);
    }

    // ============== Deserialization Benchmarks ==============

    @Benchmark
    public void foryDeserializePolicyBinding(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedPolicyBinding, PolicyBindingDto.class));
    }

    @Benchmark
    public void foryDeserializeKeyAccess(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedKeyAccess, KeyAccessDto.class));
    }

    @Benchmark
    public void foryDeserializeEntityChain(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedEntityChain, EntityChainDto.class));
    }

    @Benchmark
    public void foryDeserializeRewrapRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedRewrapRequest,
            RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void foryDeserializeDecisionResponse(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedDecisionResponse, DecisionResponseDto.class));
    }

    // ============== Round-trip Benchmarks ==============

    @Benchmark
    public void foryRoundTripPolicyBinding(Blackhole bh) {
        byte[] data = foryCodec.serialize(policyBinding);
        bh.consume(foryCodec.deserialize(data, PolicyBindingDto.class));
    }

    @Benchmark
    public void foryRoundTripKeyAccess(Blackhole bh) {
        byte[] data = foryCodec.serialize(keyAccess);
        bh.consume(foryCodec.deserialize(data, KeyAccessDto.class));
    }

    @Benchmark
    public void foryRoundTripEntityChain(Blackhole bh) {
        byte[] data = foryCodec.serialize(entityChain);
        bh.consume(foryCodec.deserialize(data, EntityChainDto.class));
    }

    @Benchmark
    public void foryRoundTripRewrapRequest(Blackhole bh) {
        byte[] data = foryCodec.serialize(rewrapRequest);
        bh.consume(foryCodec.deserialize(data, RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void foryRoundTripDecisionResponse(Blackhole bh) {
        byte[] data = foryCodec.serialize(decisionResponse);
        bh.consume(foryCodec.deserialize(data, DecisionResponseDto.class));
    }
}
