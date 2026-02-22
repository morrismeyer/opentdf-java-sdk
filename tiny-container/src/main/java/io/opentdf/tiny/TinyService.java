package io.opentdf.tiny;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Minimal HTTP service for tiny container demo.
 * Statically compiled with musl for scratch container deployment.
 *
 * This demonstrates the tiny container technique. For full Fory serialization
 * with native-image, see the FFM module's opentdf-native executable.
 */
public class TinyService {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        System.out.println("TinyService starting on port " + port);

        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                try (Socket client = server.accept()) {
                    handleRequest(client);
                } catch (Exception e) {
                    System.err.println("Request error: " + e.getMessage());
                }
            }
        }
    }

    private static void handleRequest(Socket client) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = client.getOutputStream();

        // Read HTTP request line
        String requestLine = reader.readLine();
        if (requestLine == null) return;

        // Skip headers
        while (true) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) break;
        }

        // Route handling
        String response;
        String contentType = "application/json";

        if (requestLine.startsWith("GET /health")) {
            response = "{\"status\":\"ok\"}";
        } else if (requestLine.startsWith("GET /info")) {
            response = handleInfo();
        } else if (requestLine.startsWith("GET /echo")) {
            response = handleEcho(requestLine);
        } else {
            response = "{\"endpoints\":[\"/health\",\"/info\",\"/echo?msg=hello\"]}";
        }

        // Send HTTP response
        String http = "HTTP/1.1 200 OK\r\n" +
                     "Content-Type: " + contentType + "\r\n" +
                     "Content-Length: " + response.length() + "\r\n" +
                     "Connection: close\r\n\r\n" +
                     response;
        out.write(http.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static String handleInfo() {
        return String.format(
            "{\"service\":\"TinyService\",\"version\":\"0.12.0\",\"java\":\"%s\",\"memory_mb\":%d}",
            System.getProperty("java.version"),
            Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }

    private static String handleEcho(String requestLine) {
        // Parse query string from GET /echo?msg=hello HTTP/1.1
        int msgStart = requestLine.indexOf("msg=");
        if (msgStart == -1) {
            return "{\"error\":\"missing msg parameter\"}";
        }
        int msgEnd = requestLine.indexOf(" ", msgStart);
        if (msgEnd == -1) msgEnd = requestLine.length();
        String msg = requestLine.substring(msgStart + 4, msgEnd);
        return String.format("{\"echo\":\"%s\"}", msg);
    }
}
