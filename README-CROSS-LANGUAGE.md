# OpenTDF Cross-Language Native Architecture (Java SDK)

This branch adds Java-side support for cross-language integration with Go via:

- **Apache Fory** - High-performance binary serialization for Java/Go interop
- **FFM (Foreign Function & Memory API)** - Java 25+ API for calling native code
- **UDS Client** - Java client for two-process container architecture

## New Modules

| Module | Description |
|--------|-------------|
| `fory-serialization` | Apache Fory codec and DTOs for cross-language serialization |
| `ffm` | FFM bindings to Go native library + UDS client |
| `benchmarks` | JMH benchmarks comparing Fory vs Protobuf |
| `tiny-container` | GraalVM native-image scratch container demos |

---

## Two-Process UDS Architecture

The recommended production architecture uses two static musl binaries in a scratch container, communicating via Unix Domain Socket:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Scratch Container (FROM scratch)                      │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │    opentdf-client (this module)     │      opentdf-service (Go)      │  │
│  │         Java, GraalVM               │         Go, musl               │  │
│  │                                     │                                │  │
│  │  Responsibilities:                  │  Responsibilities:             │  │
│  │  • PicoCLI frontend                 │  • TDF policy enforcement      │  │
│  │  • User interaction         UDS     │  • KAS operations              │  │
│  │  • Fory serialize     ◄───────────► │  • Authorization decisions     │  │
│  │                       /opentdf.sock │  • Fory serialization          │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  Container contents:           Security:                                    │
│  /opentdf-client   (5.7 MB)    • No network stack                           │
│  /opentdf-service  (3.5 MB)    • No shell                                   │
│  /opentdf.sock     (runtime)   • No glibc (musl static)                     │
│                                • UDS namespace-isolated                     │
│  Total: ~9.2 MB                • Separate process memory spaces             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Why Two Processes?

| Approach | Problem |
|----------|---------|
| **Static linking Go + Java** | Go runtime conflicts with GraalVM runtime (GC, signals, stack) |
| **Dynamic linking (.so)** | Requires glibc, larger image, LD_PRELOAD attack surface |
| **Two-process UDS** | Clean separation, both fully static musl, minimal attack surface |

---

## Build Instructions

### Prerequisites

- Java 25+
- GraalVM 25+ with Native Image (for native builds)
- Maven 3.9+

### Build Fory Serialization Module

```bash
mvn package -pl fory-serialization -DskipTests
```

### Build FFM Module

```bash
mvn package -pl ffm -DskipTests
```

### Build Native Image (Static musl)

```bash
# Using Docker multi-stage build
docker build -f ffm/Dockerfile.native -t opentdf-client .

# Or manually with GraalVM
native-image \
  --static --libc=musl \
  -jar ffm/target/opentdf-client.jar \
  -o opentdf-client
```

---

## Fory Configuration

Apache Fory uses annotations for field mapping. Note: Fory was renamed from Fury:

```java
public record EntityDto(
    @ForyField("ephemeralId") String ephemeralId,
    @ForyField("entityType") EntityType entityType,
    @ForyField("entityValue") String entityValue,
    @ForyField("category") Category category
) {}
```

### Cross-Language Type Registration

Types must be registered with matching names in both Java and Go:

```java
// Java
fory.register(EntityDto.class, "io.opentdf.fory.dto.EntityDto");
fory.register(EntityDto.EntityType.class, "io.opentdf.fory.dto.EntityDto$EntityType");
```

```go
// Go
codec.RegisterNamedStruct("io.opentdf.fory.dto.EntityDto", &dto.Entity{})
codec.RegisterNamedEnum("io.opentdf.fory.dto.EntityDto$EntityType", dto.EntityTypeEmailAddress)
```

---

## Benchmarks

### Run JMH Benchmarks

```bash
cd benchmarks
mvn package -DskipTests
java -jar target/benchmarks.jar
```

### Fory vs Protobuf Comparison (Java)

Results from JMH benchmarks on AMD Ryzen 7 9700X:

#### Serialization (ns/op, lower is better)

| Message Type | Fory | Protobuf | Fory Advantage |
|--------------|-----:|---------:|----------------|
| EntityChain | 145 ns | 210 ns | **1.45x faster** |
| DecisionResponse | 118 ns | 165 ns | **1.40x faster** |
| Token | 62 ns | 95 ns | **1.53x faster** |

#### Deserialization (ns/op, lower is better)

| Message Type | Fory | Protobuf | Fory Advantage |
|--------------|-----:|---------:|----------------|
| EntityChain | 198 ns | 285 ns | **1.44x faster** |
| DecisionResponse | 165 ns | 248 ns | **1.50x faster** |
| Token | 88 ns | 125 ns | **1.42x faster** |

**Key findings:**
- **Fory wins all operations** - Both serialize and deserialize
- **1.4x-1.5x faster than Protobuf** across all message types
- **Cross-language compatible** - Same binary format as Go implementation

---

## UDS Client Usage

```java
import io.opentdf.platform.ffm.UDSClient;

try (var client = new UDSClient("/opentdf.sock")) {
    // Get authorization decision
    var request = new DecisionRequestDto(actions, entityChains, resources);
    var response = client.getDecision(request);

    if (response.decision() == Decision.PERMIT) {
        // Access granted
    }
}
```

---

## GraalVM Native Image

### Required Configuration

For GraalVM native-image with Fory:

```
--initialize-at-build-time=org.apache.fory,org.slf4j
--features=org.apache.fory.graalvm.feature.ForyGraalVMFeature
--enable-native-access=ALL-UNNAMED
-H:+SharedArenaSupport
```

### Size Optimization

```
-Os                          # Optimize for size
--gc=serial                  # Smaller GC
-march=compatibility         # Broader CPU compatibility
```

After UPX compression: ~5.7 MB

---

## Related Work

See the companion changes in [opentdf-platform](https://github.com/opentdf/platform/tree/morrismeyer/opentdf-cross-language-native):
- `lib/fory` - Go Fory DTOs and codec
- `cmd/libopentdf` - CGO shared library
- `cmd/opentdf-service` - Go UDS service for two-process architecture
