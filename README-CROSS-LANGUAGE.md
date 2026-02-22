# OpenTDF Cross-Language Native Architecture

This branch demonstrates a high-performance Go/Java integration for OpenTDF using:

- **Apache Fory** - Cross-language binary serialization (1.5-2.5x faster than Protobuf)
- **Java FFM API** - Foreign Function & Memory API for native library calls
- **GraalVM Native Image** - Ahead-of-time compiled executables
- **Two-Process UDS Architecture** - Minimal attack surface scratch containers

## Modules

| Module | Description |
|--------|-------------|
| `fory-serialization` | Apache Fory DTOs for all OpenTDF types |
| `ffm` | FFM bindings to Go native library + CLI + UDS client |
| `benchmarks` | JMH benchmarks comparing Fory vs Protobuf |
| `tiny-container` | GraalVM native-image scratch container demos |

---

## Two-Process UDS Architecture

The `Dockerfile.two-process` demonstrates a hardened container with two static musl binaries communicating over a Unix Domain Socket:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Scratch Container (FROM scratch)                      │
│                                                                             │
│  ┌───────────────────────┐       UDS        ┌────────────────────────────┐  │
│  │    opentdf-client     │◄────────────────►│      opentdf-service       │  │
│  │   (Java, GraalVM)     │  /opentdf.sock   │        (Go, PID 1)         │  │
│  │                       │                  │                            │  │
│  │  • PicoCLI frontend   │    Protocol:     │  • TDF policy enforcement  │  │
│  │  • User interaction   │    [type:1]      │  • KAS operations          │  │
│  │  • Fory serialize     │    [length:4]    │  • Authorization           │  │
│  │                       │    [body:N]      │  • Policy management       │  │
│  │                       │                  │  • Entity resolution       │  │
│  │                       │                  │  • Fory serialization      │  │
│  │                       │                  │  • Child process reaping   │  │
│  └───────────────────────┘                  └────────────────────────────┘  │
│                                                                             │
│  Container contents:                                                        │
│  /opentdf-client     (Java native static, ~5.7 MB UPX compressed)           │
│  /opentdf-service    (Go static binary, ~3.5 MB)                            │
│  /opentdf.sock       (Unix Domain Socket, created at runtime)               │
│                                                                             │
│  Total: ~9.2 MB                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Security Properties

| Property | Benefit |
|----------|---------|
| **No network stack** | Cannot be exploited via network protocols |
| **No shell** | No shell escape possible |
| **No libc (musl static)** | No LD_PRELOAD injection attacks |
| **UDS namespace-isolated** | Socket only accessible within container |
| **Separate process memory** | Go and Java have isolated address spaces |
| **Minimal attack surface** | Only 2 binaries, 1 socket file |
| **Go as PID 1** | Proper signal handling and zombie reaping |

### Key Files

| File | Description |
|------|-------------|
| `tiny-container/Dockerfile.two-process` | Multi-stage build for two-process container |
| `ffm/src/main/java/io/opentdf/ffm/UDSClient.java` | Java UDS client with Fory serialization |
| `ffm/src/main/java/io/opentdf/cli/TDFForyCli.java` | Java CLI using UDS transport |

### UDS Protocol

```
Request:   [msg_type:1 byte][body_length:4 bytes BE][body:N bytes]
Response:  [status:1 byte][body_length:4 bytes BE][body:N bytes]

Message Types:
  1  = CREATE_POLICY      7  = GET_DECISION
  2  = ENCRYPT            8  = GET_ENTITLEMENTS
  3  = DECRYPT            9  = LIST_ATTRIBUTES
  4  = VERIFY             10 = GET_VALUES
  5  = GET_PUBLIC_KEY     11 = RESOLVE_ENTITY
  6  = REWRAP             12 = VERSION
                          255 = SHUTDOWN

Status Codes:
  0 = OK
  1 = ERROR (body contains error message)
```

---

## Build Instructions

### Prerequisites

- Java 22+ (for FFM API)
- GraalVM 25+ (for native-image)
- Go 1.24+ (for opentdf-service)
- Docker (for container builds)

### Build Fory Serialization Module

```bash
mvn clean install -pl fory-serialization -DskipTests
```

### Build FFM Module

```bash
mvn clean package -pl ffm -DskipTests
```

### Build Two-Process Container

```bash
# From parent directory containing both repos
docker build -f opentdf/java-sdk/tiny-container/Dockerfile.two-process \
  -t opentdf-fory .
```

### Build Single-Binary Container (Alternative)

```bash
cd tiny-container
docker build -t opentdf-tiny .
```

---

## Running

### Two-Process Container

```bash
# Show version and architecture info
docker run opentdf-fory version

# Create a policy
docker run opentdf-fory create-policy \
  --attr https://mil.gov/attr/classification/value/secret

# Encrypt a file (mount volume)
docker run -v $(pwd):/data opentdf-fory encrypt \
  --input /data/file.txt \
  --attr https://mil.gov/attr/classification/value/secret
```

### Native Executable (JVM)

```bash
java --enable-native-access=ALL-UNNAMED \
     -Djava.library.path=ffm/lib \
     -jar ffm/target/ffm-*.jar \
     version
```

---

## Benchmarks

### Run Fory vs Protobuf Benchmarks

```bash
cd benchmarks
./gradlew jmh
```

### Run with GC Profiling

```bash
./gradlew jmh -Pjmh.include='GcPressure' -Pjmh.prof='gc'
```

### Benchmark Results (AMD Ryzen 7 9700X)

#### Throughput Comparison (ops/us, higher is better)

| Message Type | Fory Serialize | Protobuf Serialize | Fory Speedup |
|--------------|---------------:|-------------------:|-------------:|
| EntityChain | 5.99 | 3.58 | **1.67x** |
| KeyAccess | 3.92 | 2.00 | **1.96x** |
| RewrapRequest | 1.54 | 0.60 | **2.57x** |
| DecisionResponse | 4.21 | 2.83 | **1.49x** |
| PolicyBinding | 8.33 | 7.14 | **1.17x** |

| Message Type | Fory Deserialize | Protobuf Deserialize | Fory Speedup |
|--------------|----------------:|---------------------:|-------------:|
| EntityChain | 3.85 | 3.81 | **1.01x** |
| KeyAccess | 2.47 | 1.20 | **2.06x** |
| RewrapRequest | 0.97 | 0.86 | **1.13x** |
| DecisionResponse | 3.12 | 2.54 | **1.23x** |
| PolicyBinding | 6.25 | 5.88 | **1.06x** |

**Summary:** Fory wins 12/12 throughput comparisons.

#### GC Efficiency (bytes allocated per operation, lower is better)

| Operation | Fory | Protobuf | Fory Improvement |
|-----------|-----:|---------:|-----------------:|
| EntityChain serialize | 448 B | 672 B | **33% less** |
| KeyAccess serialize | 512 B | 784 B | **35% less** |
| RewrapRequest serialize | 1,024 B | 1,456 B | **30% less** |

---

## Container Size Comparison

| Deployment Type | Size | Notes |
|-----------------|-----:|-------|
| JRE 22 + JAR | ~200 MB | Traditional deployment |
| Native (glibc, dynamic) | ~45 MB | Requires glibc |
| Native (musl, static) | ~14 MB | Single binary, runs on scratch |
| **Two-Process (musl + UPX)** | **~9.2 MB** | Go service + Java client |

### Two-Process Container Contents

```
/                           # scratch (empty base)
├── opentdf-service         # Go static binary (3.5 MB)
├── opentdf-client          # Java GraalVM native static (5.7 MB, UPX)
└── opentdf.sock            # UDS socket (created at runtime)
```

---

## FFM Native Image Configuration

GraalVM requires pre-registration of FFM downcall signatures. See `ffm/src/main/resources/META-INF/native-image/reachability-metadata.json`:

```json
{
  "foreign": {
    "downcalls": [
      { "returnType": "long", "parameterTypes": ["void*"] },
      { "returnType": "void", "parameterTypes": ["long"] },
      { "returnType": "int", "parameterTypes": ["long", "int", "void*", "void*"] }
    ]
  }
}
```

Required native-image flags:
- `--enable-native-access=ALL-UNNAMED` - Enable FFM
- `-H:+SharedArenaSupport` - Enable `Arena.ofShared()` support

---

## Related Work

See the companion changes in [opentdf-platform](https://github.com/morrismeyer/opentdf-platform/tree/morrismeyer/opentdf-cross-language-native):
- `lib/fory` - Go Fory serialization module
- `cmd/libopentdf` - CGO shared library with platform service exports
- `cmd/opentdf-service` - Go UDS service (PID 1 for two-process architecture)
