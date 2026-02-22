# Tiny Container Demo

Demonstrates building a minimal Java container using GraalVM Native Image with static musl linking for deployment to a `scratch` container.

## Overview

This module creates a minimal HTTP service that can be deployed in a container with:
- **No JVM** - Native executable only
- **No OS** - Runs in a `scratch` container (just the binary)
- **Static linking** - Uses musl libc for full static binary
- **~14MB binary** - Dramatically smaller than JVM-based deployments

## Building

### Prerequisites

- GraalVM 25+ with Native Image
- Docker (for container build)

### Build the JAR

```bash
mvn package -DskipTests -pl tiny-container
```

### Build Native Image (Dynamically Linked)

```bash
native-image \
  --no-fallback \
  -O3 \
  -jar tiny-container/target/tiny-service.jar \
  -o tiny-container/tiny-service
```

### Build Container (Static with musl)

```bash
docker build -t opentdf-tiny tiny-container/
```

This uses a multi-stage build:
1. Stage 1: GraalVM with musl builds a static binary
2. Stage 2: Copies binary to scratch container

## Running

### Local (dynamically linked)

```bash
./tiny-container/tiny-service 8080
```

### Docker (statically linked)

```bash
docker run -p 8080:8080 opentdf-tiny
```

## API

| Endpoint | Description |
|----------|-------------|
| `GET /health` | Health check |
| `GET /info` | Service info (version, memory) |
| `GET /echo?msg=hello` | Echo message |

## Size Comparison

| Deployment | Size |
|------------|------|
| JRE + JAR | ~200MB |
| Native (dynamic) | ~14MB |
| Native (static, musl) | ~12MB |
| Native (UPX compressed) | ~4MB |

## For Full OpenTDF Functionality

This module demonstrates the container technique. For full OpenTDF functionality with FFM and Fory serialization, see the `ffm` module's `opentdf-native` executable:

```bash
# Build FFM native image
./opentdf-native version
./opentdf-native encrypt file.txt -o file.tdf
```

The FFM module includes:
- Apache Fory serialization
- FFM bindings to Go native library
- Full KAS, Authorization, Policy operations
