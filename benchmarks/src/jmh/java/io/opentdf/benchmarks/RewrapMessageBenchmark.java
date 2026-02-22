package io.opentdf.benchmarks;

import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks specifically for the KAS rewrap message path,
 * which is the most performance-critical operation in TDF.
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, jvmArgs = {"-Xmx2g", "-XX:+UseG1GC"})
public class RewrapMessageBenchmark {

    private ForyCodec foryCodec;

    // Scenarios with different sizes
    private RewrapRequestDto.UnsignedRewrapRequestDto singlePolicyRequest;
    private RewrapRequestDto.UnsignedRewrapRequestDto multiPolicyRequest;
    private RewrapResponseDto singlePolicyResponse;
    private RewrapResponseDto multiPolicyResponse;

    private byte[] serializedSingleRequest;
    private byte[] serializedMultiRequest;
    private byte[] serializedSingleResponse;
    private byte[] serializedMultiResponse;

    @Param({"1", "5", "10"})
    private int policyCount;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Single policy request (common case)
        singlePolicyRequest = createRewrapRequest(1);
        singlePolicyResponse = createRewrapResponse(1);

        // Multi-policy request (bulk operations)
        multiPolicyRequest = createRewrapRequest(policyCount);
        multiPolicyResponse = createRewrapResponse(policyCount);

        // Pre-serialize
        serializedSingleRequest = foryCodec.serialize(singlePolicyRequest);
        serializedMultiRequest = foryCodec.serialize(multiPolicyRequest);
        serializedSingleResponse = foryCodec.serialize(singlePolicyResponse);
        serializedMultiResponse = foryCodec.serialize(multiPolicyResponse);
    }

    private RewrapRequestDto.UnsignedRewrapRequestDto createRewrapRequest(int numPolicies) {
        RewrapRequestDto.UnsignedRewrapRequestDto request = new RewrapRequestDto.UnsignedRewrapRequestDto();
        request.setClientPublicKey("-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq7cWnz...\n" +
            "-----END PUBLIC KEY-----");

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

    private RewrapResponseDto createRewrapResponse(int numPolicies) {
        RewrapResponseDto response = new RewrapResponseDto();
        response.setSessionPublicKey("-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq7cWnz...\n" +
            "-----END PUBLIC KEY-----");

        List<RewrapResponseDto.PolicyRewrapResultDto> policyResults = new ArrayList<>();
        for (int i = 0; i < numPolicies; i++) {
            RewrapResponseDto.PolicyRewrapResultDto policyResult = new RewrapResponseDto.PolicyRewrapResultDto();
            policyResult.setPolicyId("policy-" + i);

            RewrapResponseDto.KeyAccessRewrapResultDto kaoResult = new RewrapResponseDto.KeyAccessRewrapResultDto();
            kaoResult.setKeyAccessObjectId("kao-" + i);
            kaoResult.setStatus("permit");
            kaoResult.setKasWrappedKey(new byte[256]);

            policyResult.setResults(List.of(kaoResult));
            policyResults.add(policyResult);
        }
        response.setResponses(policyResults);
        return response;
    }

    // ============== Single Policy Benchmarks ==============

    @Benchmark
    public byte[] serializeSinglePolicyRequest() {
        return foryCodec.serialize(singlePolicyRequest);
    }

    @Benchmark
    public byte[] serializeSinglePolicyResponse() {
        return foryCodec.serialize(singlePolicyResponse);
    }

    @Benchmark
    public void deserializeSinglePolicyRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedSingleRequest,
            RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void deserializeSinglePolicyResponse(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedSingleResponse, RewrapResponseDto.class));
    }

    // ============== Multi-Policy Benchmarks ==============

    @Benchmark
    public byte[] serializeMultiPolicyRequest() {
        return foryCodec.serialize(multiPolicyRequest);
    }

    @Benchmark
    public byte[] serializeMultiPolicyResponse() {
        return foryCodec.serialize(multiPolicyResponse);
    }

    @Benchmark
    public void deserializeMultiPolicyRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedMultiRequest,
            RewrapRequestDto.UnsignedRewrapRequestDto.class));
    }

    @Benchmark
    public void deserializeMultiPolicyResponse(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedMultiResponse, RewrapResponseDto.class));
    }

    // ============== Full Request-Response Cycle ==============

    @Benchmark
    public void fullRewrapCycle(Blackhole bh) {
        // Serialize request
        byte[] requestData = foryCodec.serialize(multiPolicyRequest);
        // Deserialize request (as if received by KAS)
        RewrapRequestDto.UnsignedRewrapRequestDto request =
            foryCodec.deserialize(requestData, RewrapRequestDto.UnsignedRewrapRequestDto.class);
        bh.consume(request);

        // Serialize response
        byte[] responseData = foryCodec.serialize(multiPolicyResponse);
        // Deserialize response (as if received by client)
        RewrapResponseDto response =
            foryCodec.deserialize(responseData, RewrapResponseDto.class);
        bh.consume(response);
    }
}
