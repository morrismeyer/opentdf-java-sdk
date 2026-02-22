#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "=== Building Tiny Container ==="

# Build the JAR
echo "[1/2] Building JAR..."
mvn package -DskipTests -pl tiny-container -Denforcer.skip=true -q

# Find native-image
NATIVE_IMAGE="${GRAALVM_HOME:-}/bin/native-image"
if [ ! -x "$NATIVE_IMAGE" ]; then
    NATIVE_IMAGE=$(which native-image 2>/dev/null || true)
fi

if [ -z "$NATIVE_IMAGE" ]; then
    echo "Error: native-image not found. Set GRAALVM_HOME or add to PATH."
    echo "Download GraalVM from: https://www.graalvm.org/downloads/"
    exit 1
fi

# Build native image
echo "[2/2] Building native image..."
cd tiny-container

$NATIVE_IMAGE \
    --no-fallback \
    -O3 \
    -jar target/tiny-service.jar \
    -o tiny-service

# Show results
echo ""
echo "=== Build Complete ==="
ls -lh tiny-service
file tiny-service

echo ""
echo "Test with:"
echo "  ./tiny-container/tiny-service 8080"
echo "  curl http://localhost:8080/health"
echo ""
echo "Build Docker container:"
echo "  docker build -t opentdf-tiny tiny-container/"
echo "  docker run -p 8080:8080 opentdf-tiny"
