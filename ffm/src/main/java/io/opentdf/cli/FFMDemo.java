package io.opentdf.cli;

import io.opentdf.ffm.OpenTDFPlatform;
import io.opentdf.ffm.KASClient;
import io.opentdf.fory.dto.*;

import java.util.List;

/**
 * Demonstration of OpenTDF FFM integration with Go native library.
 * This shows the full round-trip: Java -> FFM -> Go -> Fory -> Java
 */
public class FFMDemo {

    public static void main(String[] args) {
        System.out.println("=== OpenTDF FFM Demo ===");
        System.out.println("Java " + System.getProperty("java.version"));
        System.out.println();

        String config = """
            {
                "platformEndpoint": "http://localhost:8080",
                "clientId": "demo-client",
                "clientSecret": "demo-secret"
            }
            """;

        try (OpenTDFPlatform platform = new OpenTDFPlatform(config)) {
            System.out.println("[OK] Platform initialized");

            // Get version
            String version = platform.getVersion();
            System.out.println("[OK] Platform version: " + version);

            // Test KAS - Get public key
            System.out.println();
            System.out.println("--- KAS Operations ---");
            PublicKeyResponseDto publicKey = platform.kas().getPublicKey(KASClient.Algorithm.RSA_2048);
            System.out.println("[OK] Got public key: " + publicKey.getKid());

            // Note: Authorization, Policy, and Entity Resolution operations require
            // schema compatibility between Java and Go Fory, which is still being debugged.
            // For this demo, we've verified that:
            // 1. Platform initialization works (FFM -> Go)
            // 2. Version retrieval works (Go -> Java)
            // 3. KAS public key retrieval works (Go serialization -> Java deserialization)

            System.out.println();
            System.out.println("=== All FFM operations successful! ===");

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
