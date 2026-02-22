# Tiny Java Container with GraalVM Native Image
#
# Build a minimal scratch container with a static native image.
#
# Usage:
#   # Build JAR first
#   mvn package -DskipTests -pl tiny-container
#
#   # Build container
#   docker build -t opentdf-tiny tiny-container/
#
#   # Run
#   docker run -p 8080:8080 opentdf-tiny

# Stage 1: Build static native image with musl
FROM ghcr.io/graalvm/native-image-community:25-muslib AS builder

WORKDIR /build

# Copy JAR (build with mvn package first)
COPY target/tiny-service.jar .

# Build static native image with musl for scratch container
RUN native-image \
    --static --libc=musl \
    --no-fallback \
    -O3 \
    -jar tiny-service.jar \
    -o tiny-service

# Show binary info
RUN ls -lh tiny-service && file tiny-service

# Stage 2: Scratch container (empty - just our binary)
FROM scratch

# Copy the static binary
COPY --from=builder /build/tiny-service /tiny-service

# Service port
EXPOSE 8080

# Run the service
ENTRYPOINT ["/tiny-service"]
CMD ["8080"]
