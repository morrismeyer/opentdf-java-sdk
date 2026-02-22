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
 * Benchmarks for batch/bulk serialization operations.
 * Measures performance when processing multiple objects together.
 */
@State(Scope.Thread)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 2, jvmArgs = {"-Xmx2g", "-XX:+UseG1GC"})
public class BatchSerializationBenchmark {

    private ForyCodec foryCodec;

    private List<EntityChainDto> entityChainBatch;
    private List<DecisionResponseDto> decisionBatch;
    private List<KeyAccessDto> keyAccessBatch;

    private byte[] serializedEntityChainBatch;
    private byte[] serializedDecisionBatch;
    private byte[] serializedKeyAccessBatch;

    @Param({"10", "50", "100", "500"})
    private int batchSize;

    @Setup(Level.Trial)
    public void setup() {
        foryCodec = new ForyCodec();

        // Create batches of different sizes
        entityChainBatch = createEntityChainBatch(batchSize);
        decisionBatch = createDecisionBatch(batchSize);
        keyAccessBatch = createKeyAccessBatch(batchSize);

        // Pre-serialize batches
        serializedEntityChainBatch = foryCodec.serialize(entityChainBatch);
        serializedDecisionBatch = foryCodec.serialize(decisionBatch);
        serializedKeyAccessBatch = foryCodec.serialize(keyAccessBatch);
    }

    private List<EntityChainDto> createEntityChainBatch(int size) {
        List<EntityChainDto> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            EntityDto entity = new EntityDto(
                "entity-" + i,
                EntityDto.EntityType.EMAIL_ADDRESS,
                "user" + i + "@example.com",
                EntityDto.Category.SUBJECT
            );
            // Use array constructor for optimized serialization (Section 12a)
            batch.add(new EntityChainDto("chain-" + i, new EntityDto[]{entity}));
        }
        return batch;
    }

    private List<DecisionResponseDto> createDecisionBatch(int size) {
        List<DecisionResponseDto> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            DecisionResponseDto response = new DecisionResponseDto();
            response.setEntityChainId("ec-" + i);
            response.setResourceAttributesId("res-" + i);
            response.setDecision(i % 2 == 0 ? DecisionResponseDto.Decision.PERMIT : DecisionResponseDto.Decision.DENY);
            response.setAction(ActionDto.standard(ActionDto.StandardAction.TRANSMIT));
            if (response.isPermit()) {
                response.setObligations(Arrays.asList(
                    "http://example.org/obligation/audit"
                ));
            }
            batch.add(response);
        }
        return batch;
    }

    private List<KeyAccessDto> createKeyAccessBatch(int size) {
        List<KeyAccessDto> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            KeyAccessDto keyAccess = new KeyAccessDto();
            keyAccess.setKeyType("wrapped");
            keyAccess.setKasUrl("https://kas.example.com");
            keyAccess.setKid("key-" + i);
            keyAccess.setProtocol("kas");
            keyAccess.setWrappedKey(new byte[256]);
            keyAccess.setPolicyBinding(new PolicyBindingDto("HS256", "hash-" + i));
            batch.add(keyAccess);
        }
        return batch;
    }

    // ============== Batch Serialization ==============

    @Benchmark
    public byte[] serializeEntityChainBatch() {
        return foryCodec.serialize(entityChainBatch);
    }

    @Benchmark
    public byte[] serializeDecisionBatch() {
        return foryCodec.serialize(decisionBatch);
    }

    @Benchmark
    public byte[] serializeKeyAccessBatch() {
        return foryCodec.serialize(keyAccessBatch);
    }

    // ============== Batch Deserialization ==============

    @Benchmark
    @SuppressWarnings("unchecked")
    public void deserializeEntityChainBatch(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedEntityChainBatch, List.class));
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void deserializeDecisionBatch(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedDecisionBatch, List.class));
    }

    @Benchmark
    @SuppressWarnings("unchecked")
    public void deserializeKeyAccessBatch(Blackhole bh) {
        bh.consume(foryCodec.deserialize(serializedKeyAccessBatch, List.class));
    }

    // ============== Individual vs Batch Comparison ==============

    /**
     * Serialize objects one at a time for comparison.
     */
    @Benchmark
    public void serializeIndividually(Blackhole bh) {
        for (DecisionResponseDto response : decisionBatch) {
            bh.consume(foryCodec.serialize(response));
        }
    }

    /**
     * Deserialize objects one at a time.
     */
    @Benchmark
    public void deserializeIndividually(Blackhole bh) {
        // First serialize each one
        List<byte[]> serializedItems = new ArrayList<>(batchSize);
        for (DecisionResponseDto response : decisionBatch) {
            serializedItems.add(foryCodec.serialize(response));
        }

        // Then deserialize
        for (byte[] data : serializedItems) {
            bh.consume(foryCodec.deserialize(data, DecisionResponseDto.class));
        }
    }

    // ============== Throughput Per Item ==============

    /**
     * Measures per-item throughput when batch processing.
     */
    @Benchmark
    @OperationsPerInvocation(1) // Will be multiplied by batchSize in analysis
    public byte[] batchSerializeThroughput() {
        return foryCodec.serialize(decisionBatch);
    }

    // ============== Size Metrics ==============

    /**
     * Reports serialized size for analysis.
     * Not a performance benchmark, just for size reporting.
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public int measureSerializedSize() {
        return serializedDecisionBatch.length;
    }
}
