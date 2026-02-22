package io.opentdf.benchmarks;

import com.google.protobuf.ByteString;
import io.opentdf.benchmarks.proto.*;
import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Head-to-head benchmark comparing Apache Fory vs Google Protobuf serialization.
 * Tests identical message structures to provide fair comparison.
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, jvmArgs = {"-Xmx2g", "-XX:+UseG1GC"})
public class ForyVsProtobufBenchmark {

    private ForyCodec foryCodec;

    // === Fory DTOs ===
    private PolicyBindingDto foryPolicyBinding;
    private KeyAccessDto foryKeyAccess;
    private EntityChainDto foryEntityChain;
    private RewrapRequestDto.UnsignedRewrapRequestDto foryRewrapRequest;
    private DecisionResponseDto foryDecisionResponse;

    // === Protobuf Messages ===
    private PolicyBindingProto protoPolicyBinding;
    private KeyAccessProto protoKeyAccess;
    private EntityChainProto protoEntityChain;
    private UnsignedRewrapRequestProto protoRewrapRequest;
    private DecisionResponseProto protoDecisionResponse;

    // === Pre-serialized data ===
    private byte[] forySerializedPolicyBinding;
    private byte[] forySerializedKeyAccess;
    private byte[] forySerializedEntityChain;
    private byte[] forySerializedRewrapRequest;
    private byte[] forySerializedDecisionResponse;

    private byte[] protoSerializedPolicyBinding;
    private byte[] protoSerializedKeyAccess;
    private byte[] protoSerializedEntityChain;
    private byte[] protoSerializedRewrapRequest;
    private byte[] protoSerializedDecisionResponse;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Create matching test data for both frameworks
        setupForyData();
        setupProtobufData();

        // Pre-serialize for deserialization benchmarks
        forySerializedPolicyBinding = foryCodec.serialize(foryPolicyBinding);
        forySerializedKeyAccess = foryCodec.serialize(foryKeyAccess);
        forySerializedEntityChain = foryCodec.serialize(foryEntityChain);
        forySerializedRewrapRequest = foryCodec.serialize(foryRewrapRequest);
        forySerializedDecisionResponse = foryCodec.serialize(foryDecisionResponse);

        protoSerializedPolicyBinding = protoPolicyBinding.toByteArray();
        protoSerializedKeyAccess = protoKeyAccess.toByteArray();
        protoSerializedEntityChain = protoEntityChain.toByteArray();
        protoSerializedRewrapRequest = protoRewrapRequest.toByteArray();
        protoSerializedDecisionResponse = protoDecisionResponse.toByteArray();
    }

    private void setupForyData() {
        foryPolicyBinding = new PolicyBindingDto("HS256", "abc123hashvalue456def");

        foryKeyAccess = new KeyAccessDto();
        foryKeyAccess.setKeyType("wrapped");
        foryKeyAccess.setKasUrl("https://kas.example.com/api/v1");
        foryKeyAccess.setKid("key-identifier-12345");
        foryKeyAccess.setProtocol("kas");
        foryKeyAccess.setWrappedKey(new byte[256]);
        foryKeyAccess.setPolicyBinding(foryPolicyBinding);
        foryKeyAccess.setEncryptedMetadata("base64encodedmetadata==");

        EntityDto entity1 = new EntityDto("e1", EntityDto.EntityType.EMAIL_ADDRESS,
            "bob@example.com", EntityDto.Category.SUBJECT);
        EntityDto entity2 = new EntityDto("e2", EntityDto.EntityType.CLIENT_ID,
            "client-application-123", EntityDto.Category.ENVIRONMENT);
        // Use array constructor for optimized serialization (Section 12a: Contiguous Storage)
        foryEntityChain = new EntityChainDto("chain-1", new EntityDto[]{entity1, entity2});

        foryRewrapRequest = new RewrapRequestDto.UnsignedRewrapRequestDto();
        foryRewrapRequest.setClientPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----");

        RewrapRequestDto.WithKeyAccessObjectDto kao = new RewrapRequestDto.WithKeyAccessObjectDto();
        kao.setKeyAccessObjectId("kao-1");
        kao.setKeyAccessObject(foryKeyAccess);

        RewrapRequestDto.WithPolicyDto policy = new RewrapRequestDto.WithPolicyDto("policy-1",
            "eyJhdHRyaWJ1dGVzIjpbeyJmcW4iOiJodHRwczovL2V4YW1wbGUuY29tL2F0dHIvY2xhc3NpZmljYXRpb24vdmFsdWUvc2VjcmV0In1dfQ==");

        RewrapRequestDto.WithPolicyRequestDto policyRequest = new RewrapRequestDto.WithPolicyRequestDto();
        policyRequest.setKeyAccessObjects(Arrays.asList(kao));
        policyRequest.setPolicy(policy);
        policyRequest.setAlgorithm("rsa:2048");
        foryRewrapRequest.setRequests(Arrays.asList(policyRequest));

        foryDecisionResponse = new DecisionResponseDto();
        foryDecisionResponse.setEntityChainId("ec1");
        foryDecisionResponse.setResourceAttributesId("attr-set-1");
        foryDecisionResponse.setDecision(DecisionResponseDto.Decision.PERMIT);
        foryDecisionResponse.setObligations(Arrays.asList(
            "http://example.org/obligation/watermark",
            "http://example.org/obligation/audit"
        ));
        foryDecisionResponse.setAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));
    }

    private void setupProtobufData() {
        protoPolicyBinding = PolicyBindingProto.newBuilder()
            .setAlgorithm("HS256")
            .setHash("abc123hashvalue456def")
            .build();

        protoKeyAccess = KeyAccessProto.newBuilder()
            .setKeyType("wrapped")
            .setKasUrl("https://kas.example.com/api/v1")
            .setKid("key-identifier-12345")
            .setProtocol("kas")
            .setWrappedKey(ByteString.copyFrom(new byte[256]))
            .setPolicyBinding(protoPolicyBinding)
            .setEncryptedMetadata("base64encodedmetadata==")
            .build();

        protoEntityChain = EntityChainProto.newBuilder()
            .setId("chain-1")
            .addEntities(EntityProto.newBuilder()
                .setId("e1")
                .setEmailAddress("bob@example.com")
                .setCategory(EntityProto.Category.CATEGORY_SUBJECT)
                .build())
            .addEntities(EntityProto.newBuilder()
                .setId("e2")
                .setClientId("client-application-123")
                .setCategory(EntityProto.Category.CATEGORY_ENVIRONMENT)
                .build())
            .build();

        protoRewrapRequest = UnsignedRewrapRequestProto.newBuilder()
            .setClientPublicKey("-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----")
            .addRequests(WithPolicyRequestProto.newBuilder()
                .addKeyAccessObjects(WithKeyAccessObjectProto.newBuilder()
                    .setKeyAccessObjectId("kao-1")
                    .setKeyAccessObject(protoKeyAccess)
                    .build())
                .setPolicy(WithPolicyProto.newBuilder()
                    .setId("policy-1")
                    .setBody("eyJhdHRyaWJ1dGVzIjpbeyJmcW4iOiJodHRwczovL2V4YW1wbGUuY29tL2F0dHIvY2xhc3NpZmljYXRpb24vdmFsdWUvc2VjcmV0In1dfQ==")
                    .build())
                .setAlgorithm("rsa:2048")
                .build())
            .build();

        protoDecisionResponse = DecisionResponseProto.newBuilder()
            .setEntityChainId("ec1")
            .setResourceAttributesId("attr-set-1")
            .setDecision(DecisionResponseProto.Decision.DECISION_PERMIT)
            .addObligations("http://example.org/obligation/watermark")
            .addObligations("http://example.org/obligation/audit")
            .setAction(ActionProto.newBuilder()
                .setStandard(ActionProto.StandardAction.STANDARD_ACTION_TRANSMIT)
                .build())
            .build();
    }

    // ============== PolicyBinding Benchmarks ==============

    @Benchmark
    public byte[] forySerializePolicyBinding() {
        return foryCodec.serialize(foryPolicyBinding);
    }

    @Benchmark
    public byte[] protobufSerializePolicyBinding() {
        return protoPolicyBinding.toByteArray();
    }

    @Benchmark
    public void foryDeserializePolicyBinding(Blackhole bh) {
        bh.consume(foryCodec.deserialize(forySerializedPolicyBinding, PolicyBindingDto.class));
    }

    @Benchmark
    public void protobufDeserializePolicyBinding(Blackhole bh) throws Exception {
        bh.consume(PolicyBindingProto.parseFrom(protoSerializedPolicyBinding));
    }

    // ============== KeyAccess Benchmarks ==============

    @Benchmark
    public byte[] forySerializeKeyAccess() {
        return foryCodec.serialize(foryKeyAccess);
    }

    @Benchmark
    public byte[] protobufSerializeKeyAccess() {
        return protoKeyAccess.toByteArray();
    }

    @Benchmark
    public void foryDeserializeKeyAccess(Blackhole bh) {
        bh.consume(foryCodec.deserialize(forySerializedKeyAccess, KeyAccessDto.class));
    }

    @Benchmark
    public void protobufDeserializeKeyAccess(Blackhole bh) throws Exception {
        bh.consume(KeyAccessProto.parseFrom(protoSerializedKeyAccess));
    }

    // ============== EntityChain Benchmarks ==============

    @Benchmark
    public byte[] forySerializeEntityChain() {
        return foryCodec.serialize(foryEntityChain);
    }

    @Benchmark
    public byte[] protobufSerializeEntityChain() {
        return protoEntityChain.toByteArray();
    }

    @Benchmark
    public void foryDeserializeEntityChain(Blackhole bh) {
        bh.consume(foryCodec.deserialize(forySerializedEntityChain, EntityChainDto.class));
    }

    @Benchmark
    public void protobufDeserializeEntityChain(Blackhole bh) throws Exception {
        bh.consume(EntityChainProto.parseFrom(protoSerializedEntityChain));
    }

    // ============== RewrapRequest Benchmarks ==============

    @Benchmark
    public byte[] forySerializeRewrapRequest() {
        return foryCodec.serialize(foryRewrapRequest);
    }

    @Benchmark
    public byte[] protobufSerializeRewrapRequest() {
        return protoRewrapRequest.toByteArray();
    }

    @Benchmark
    public void foryDeserializeRewrapRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(forySerializedRewrapRequest,
            RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void protobufDeserializeRewrapRequest(Blackhole bh) throws Exception {
        bh.consume(UnsignedRewrapRequestProto.parseFrom(protoSerializedRewrapRequest));
    }

    // ============== DecisionResponse Benchmarks ==============

    @Benchmark
    public byte[] forySerializeDecisionResponse() {
        return foryCodec.serialize(foryDecisionResponse);
    }

    @Benchmark
    public byte[] protobufSerializeDecisionResponse() {
        return protoDecisionResponse.toByteArray();
    }

    @Benchmark
    public void foryDeserializeDecisionResponse(Blackhole bh) {
        bh.consume(foryCodec.deserialize(forySerializedDecisionResponse, DecisionResponseDto.class));
    }

    @Benchmark
    public void protobufDeserializeDecisionResponse(Blackhole bh) throws Exception {
        bh.consume(DecisionResponseProto.parseFrom(protoSerializedDecisionResponse));
    }

    // ============== Round-trip Benchmarks ==============

    @Benchmark
    public void foryRoundTripKeyAccess(Blackhole bh) {
        byte[] data = foryCodec.serialize(foryKeyAccess);
        bh.consume(foryCodec.deserialize(data, KeyAccessDto.class));
    }

    @Benchmark
    public void protobufRoundTripKeyAccess(Blackhole bh) throws Exception {
        byte[] data = protoKeyAccess.toByteArray();
        bh.consume(KeyAccessProto.parseFrom(data));
    }

    @Benchmark
    public void foryRoundTripRewrapRequest(Blackhole bh) {
        byte[] data = foryCodec.serialize(foryRewrapRequest);
        bh.consume(foryCodec.deserialize(data, RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void protobufRoundTripRewrapRequest(Blackhole bh) throws Exception {
        byte[] data = protoRewrapRequest.toByteArray();
        bh.consume(UnsignedRewrapRequestProto.parseFrom(data));
    }

    // ============== Size Comparison (sanity check, not timed) ==============

    @Benchmark
    public int measureForySize() {
        return forySerializedKeyAccess.length +
               forySerializedRewrapRequest.length +
               forySerializedDecisionResponse.length;
    }

    @Benchmark
    public int measureProtobufSize() {
        return protoSerializedKeyAccess.length +
               protoSerializedRewrapRequest.length +
               protoSerializedDecisionResponse.length;
    }
}
