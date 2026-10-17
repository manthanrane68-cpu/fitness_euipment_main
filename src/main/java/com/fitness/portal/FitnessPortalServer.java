package com.fitness.portal;

import com.fitness.portal.controller.ApiController;
import com.fitness.portal.repository.RecordRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * Main application entrypoint: initializes the HTTP server, REST controllers,
 * and static presentation assets.
 */
public class FitnessPortalServer {
    private static final int DEFAULT_PORT = 8080;
    private static HttpServer server;

    public static void main(String[] args) {
        int port = determinePort();
        try {
            startServer(port);
            System.out.println("==================================================================");
            System.out.println("  FITNESS EQUIPMENT MAINTENANCE PORTAL (DevOps MVP v1.0.0)");
            System.out.println("  Server successfully started on port: " + port);
            System.out.println("  Web UI:      http://localhost:" + port + "/");
            System.out.println("  Health API:  http://localhost:" + port + "/api/health");
            System.out.println("  Records API: http://localhost:" + port + "/api/records");
            System.out.println("==================================================================");
        } catch (IOException e) {
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static synchronized HttpServer startServer(int port) throws IOException {
        if (server != null) {
            return server;
        }

        RecordRepository repository = new RecordRepository();
        ApiController apiController = new ApiController(repository);

        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Context Handler
        server.createContext("/api", apiController::handle);

        // Static Asset Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(Executors.newFixedThreadPool(32));
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping Fitness Portal Server...");
            server.stop(1);
        }));

        return server;
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static int determinePort() {
        String prop = System.getProperty("server.port");
        if (prop != null && !prop.trim().isEmpty()) {
            try { return Integer.parseInt(prop.trim()); } catch (NumberFormatException ignored) {}
        }
        String env = System.getenv("PORT");
        if (env != null && !env.trim().isEmpty()) {
            try { return Integer.parseInt(env.trim()); } catch (NumberFormatException ignored) {}
        }
        return DEFAULT_PORT;
    }

    /**
     * Handler to serve web UI static assets (HTML, CSS, JS) from classpath or local disk.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Prevent path traversal
            if (path.contains("..")) {
                send404(exchange);
                return;
            }

            byte[] content = loadAsset(path);
            if (content == null) {
                send404(exchange);
                return;
            }

            String contentType = determineContentType(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, must-revalidate");
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(content);
            }
        }

        private byte[] loadAsset(String path) {
            // First try classpath resource: /web/index.html etc.
            String resourcePath = "/web" + path;
            try (InputStream in = FitnessPortalServer.class.getResourceAsStream(resourcePath)) {
                if (in != null) {
                    return in.readAllBytes();
                }
            } catch (IOException ignored) {}

            // Fallback to local files under src/main/resources/web or web
            Path[] potentialPaths = new Path[]{
                    Paths.get("src/main/resources/web", path),
                    Paths.get("target/classes/web", path),
                    Paths.get("web", path)
            };

            for (Path p : potentialPaths) {
                if (Files.isRegularFile(p)) {
                    try {
                        return Files.readAllBytes(p);
                    } catch (IOException ignored) {}
                }
            }

            return null;
        }

        private String determineContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }

        private void send404(HttpExchange exchange) throws IOException {
            String msg = "404 Not Found";
            exchange.sendResponseHeaders(404, msg.length());
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(msg.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
