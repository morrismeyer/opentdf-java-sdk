package io.opentdf.benchmarks;

import io.opentdf.fory.ForyCodec;
import io.opentdf.fory.dto.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for authorization decision request/response serialization.
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, jvmArgs = {"-Xmx2g", "-XX:+UseG1GC"})
public class AuthorizationBenchmark {

    private ForyCodec foryCodec;

    // Simple request (1 entity, 1 resource)
    private DecisionRequestDto simpleRequest;
    // Complex request (multiple entities, multiple resources)
    private DecisionRequestDto complexRequest;

    // Single decision response
    private DecisionResponseDto singleResponse;
    // Multiple decision responses (batch)
    private List<DecisionResponseDto> batchResponses;

    private byte[] serializedSimpleRequest;
    private byte[] serializedComplexRequest;
    private byte[] serializedSingleResponse;
    private byte[] serializedBatchResponses;

    @Param({"1", "5", "10"})
    private int entityCount;

    @Param({"1", "3", "5"})
    private int resourceCount;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Simple request
        simpleRequest = createDecisionRequest(1, 1);

        // Complex request based on parameters
        complexRequest = createDecisionRequest(entityCount, resourceCount);

        // Single response
        singleResponse = createDecisionResponse("ec1", "res1", DecisionResponseDto.Decision.PERMIT);

        // Batch responses
        batchResponses = createBatchResponses(entityCount, resourceCount);

        // Pre-serialize
        serializedSimpleRequest = foryCodec.serialize(simpleRequest);
        serializedComplexRequest = foryCodec.serialize(complexRequest);
        serializedSingleResponse = foryCodec.serialize(singleResponse);
        serializedBatchResponses = foryCodec.serialize(batchResponses);
    }

    private DecisionRequestDto createDecisionRequest(int numEntities, int numResources) {
        DecisionRequestDto request = new DecisionRequestDto();

        // Add action
        request.addAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));

        // Add entity chains (using array constructor for optimized serialization)
        for (int i = 0; i < numEntities; i++) {
            EntityDto entity = new EntityDto(
                "entity-" + i,
                EntityDto.EntityType.EMAIL_ADDRESS,
                "user" + i + "@example.com",
                EntityDto.Category.SUBJECT
            );
            request.addEntityChain(new EntityChainDto("ec-" + i, new EntityDto[]{entity}));
        }

        // Add resources
        for (int i = 0; i < numResources; i++) {
            request.addResourceAttribute(new ResourceDto(
                "res-" + i,
                Arrays.asList(
                    "https://example.com/attr/classification/value/secret",
                    "https://example.com/attr/region/value/us"
                )
            ));
        }

        return request;
    }

    private DecisionResponseDto createDecisionResponse(String entityChainId, String resourceId,
                                                        DecisionResponseDto.Decision decision) {
        DecisionResponseDto response = new DecisionResponseDto();
        response.setEntityChainId(entityChainId);
        response.setResourceAttributesId(resourceId);
        response.setDecision(decision);
        response.setAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));
        if (decision == DecisionResponseDto.Decision.PERMIT) {
            response.setObligations(Arrays.asList(
                "http://example.org/obligation/watermark",
                "http://example.org/obligation/audit"
            ));
        }
        return response;
    }

    private List<DecisionResponseDto> createBatchResponses(int numEntities, int numResources) {
        List<DecisionResponseDto> responses = new ArrayList<>();
        for (int e = 0; e < numEntities; e++) {
            for (int r = 0; r < numResources; r++) {
                DecisionResponseDto.Decision decision =
                    (e + r) % 2 == 0 ? DecisionResponseDto.Decision.PERMIT : DecisionResponseDto.Decision.DENY;
                responses.add(createDecisionResponse("ec-" + e, "res-" + r, decision));
            }
        }
        return responses;
    }

    // ============== Request Benchmarks ==============

    @Benchmark
    public byte[] serializeSimpleRequest() {
        return foryCodec.serialize(simpleRequest);
    }

    @Benchmark
    public byte[] serializeComplexRequest() {
        return foryCodec.serialize(complexRequest);
    }

    @Benchmark
    public void deserializeSimpleRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedSimpleRequest, DecisionRequestDto.class));
    }

    @Benchmark
    public void deserializeComplexRequest(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedComplexRequest, DecisionRequestDto.class));
    }

    // ============== Response Benchmarks ==============

    @Benchmark
    public byte[] serializeSingleResponse() {
        return foryCodec.serialize(singleResponse);
    }

    @Benchmark
    public byte[] serializeBatchResponses() {
        return foryCodec.serialize(batchResponses);
    }

    @Benchmark
    public void deserializeSingleResponse(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedSingleResponse, DecisionResponseDto.class));
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void deserializeBatchResponses(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedBatchResponses, List.class));
    }

    // ============== Full Request-Response Cycle ==============

    @Benchmark
    public void fullAuthorizationCycle(Blackhole bh) {
        // Client serializes request
        byte[] requestData = foryCodec.serialize(complexRequest);

        // Server deserializes request
        DecisionRequestDto request = foryCodec.deserialize(requestData, DecisionRequestDto.class);
        bh.consume(request);

        // Server serializes responses
        byte[] responseData = foryCodec.serialize(batchResponses);

        // Client deserializes responses
        @SuppressWarnings("unchecked")
        List<DecisionResponseDto> responses = foryCodec.deserialize(responseData, List.class);
        bh.consume(responses);
    }
}
