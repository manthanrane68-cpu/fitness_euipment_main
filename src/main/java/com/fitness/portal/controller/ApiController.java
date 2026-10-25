package com.fitness.portal.controller;

import com.fitness.portal.model.DashboardStats;
import com.fitness.portal.model.MaintenanceRecord;
import com.fitness.portal.model.User;
import com.fitness.portal.repository.RecordRepository;
import com.fitness.portal.repository.UserRepository;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller handling REST API calls under /api/*
 * Includes equipment records, operational KPIs, and user authentication endpoints.
 */
public class ApiController {
    private final RecordRepository repository;
    private final UserRepository userRepository;
    private final long startTime = System.currentTimeMillis();

    public ApiController(RecordRepository repository) {
        this(repository, new UserRepository());
    }

    public ApiController(RecordRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        // Handle CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        try {
            if (path.equals("/api/health") && "GET".equalsIgnoreCase(method)) {
                handleHealth(exchange);
            } else if (path.equals("/api/dashboard") && "GET".equalsIgnoreCase(method)) {
                handleDashboard(exchange);
            } else if (path.equals("/api/reset") && "POST".equalsIgnoreCase(method)) {
                repository.reset();
                userRepository.reset();
                sendJsonResponse(exchange, 200, "{\"status\":\"ok\",\"message\":\"Database reset to initial seed\"}");
            } else if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
                handleLogin(exchange);
            } else if (path.equals("/api/auth/register") && "POST".equalsIgnoreCase(method)) {
                handleRegister(exchange);
            } else if (path.equals("/api/auth/me") && "GET".equalsIgnoreCase(method)) {
                handleAuthMe(exchange, query);
            } else if (path.equals("/api/auth/logout") && ("POST".equalsIgnoreCase(method) || "GET".equalsIgnoreCase(method))) {
                handleLogout(exchange, query);
            } else if (path.equals("/api/users") && "GET".equalsIgnoreCase(method)) {
                handleListUsers(exchange);
            } else if (path.equals("/api/records")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleListRecords(exchange, query);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleCreateRecord(exchange);
                } else {
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } else if (path.startsWith("/api/records/")) {
                String id = path.substring("/api/records/".length());
                if ("GET".equalsIgnoreCase(method)) {
                    handleGetRecord(exchange, id);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    handleUpdateRecord(exchange, id);
                } else if ("DELETE".equalsIgnoreCase(method)) {
                    handleDeleteRecord(exchange, id);
                } else {
                    sendResponse(exchange, 405, "Method Not Allowed");
                }
            } else {
                sendJsonResponse(exchange, 404, "{\"error\":\"Not Found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeap = memBean.getHeapMemoryUsage().getMax() / (1024 * 1024);

        String json = "{"
                + "\"status\":\"UP\","
                + "\"service\":\"Fitness Equipment Maintenance Portal\","
                + "\"version\":\"1.0.0-mvp\","
                + "\"uptimeSeconds\":" + uptimeSeconds + ","
                + "\"heapMemoryMb\":\"" + usedHeap + "MB / " + maxHeap + "MB\","
                + "\"activeRecords\":" + repository.getAll().size() + ","
                + "\"registeredUsers\":" + userRepository.getAllUsers().size() + ","
                + "\"databaseStatus\":{"
                + "\"recordsDb\":\"data/maintenance_records.json [ONLINE]\","
                + "\"usersDb\":\"data/users.json [ONLINE]\""
                + "}"
                + "}";
        sendJsonResponse(exchange, 200, json);
    }

    private void handleDashboard(HttpExchange exchange) throws IOException {
        DashboardStats stats = repository.getDashboardStats();
        sendJsonResponse(exchange, 200, stats.toJson());
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String username = extractString(body, "username");
        String password = extractString(body, "password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Username and password are required\"}");
            return;
        }

        UserRepository.AuthResult result = userRepository.authenticate(username, password);
        if (result.isSuccess()) {
            String json = "{"
                    + "\"status\":\"success\","
                    + "\"message\":\"Login successful\","
                    + "\"token\":\"" + result.getToken() + "\","
                    + "\"user\":" + result.getUser().toJson()
                    + "}";
            sendJsonResponse(exchange, 200, json);
        } else {
            sendJsonResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"" + result.getMessage() + "\"}");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        String username = extractString(body, "username");
        String password = extractString(body, "password");
        String fullName = extractString(body, "fullName");
        String email = extractString(body, "email");
        String role = extractString(body, "role");

        if (username == null || username.trim().length() < 3) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Username must be at least 3 characters\"}");
            return;
        }
        if (password == null || password.trim().length() < 5) {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"Password must be at least 5 characters\"}");
            return;
        }

        UserRepository.AuthResult result = userRepository.register(username, password, fullName, email, role);
        if (result.isSuccess()) {
            String json = "{"
                    + "\"status\":\"success\","
                    + "\"message\":\"User registered successfully\","
                    + "\"token\":\"" + result.getToken() + "\","
                    + "\"user\":" + result.getUser().toJson()
                    + "}";
            sendJsonResponse(exchange, 201, json);
        } else {
            sendJsonResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + result.getMessage() + "\"}");
        }
    }

    private void handleAuthMe(HttpExchange exchange, String query) throws IOException {
        String token = extractAuthToken(exchange, query);
        if (token == null) {
            sendJsonResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"No authorization token provided\"}");
            return;
        }

        Optional<User> userOpt = userRepository.getUserByToken(token);
        if (userOpt.isPresent()) {
            String json = "{"
                    + "\"status\":\"success\","
                    + "\"user\":" + userOpt.get().toJson()
                    + "}";
            sendJsonResponse(exchange, 200, json);
        } else {
            sendJsonResponse(exchange, 401, "{\"status\":\"error\",\"message\":\"Invalid or expired session token\"}");
        }
    }

    private void handleLogout(HttpExchange exchange, String query) throws IOException {
        String token = extractAuthToken(exchange, query);
        if (token != null) {
            userRepository.invalidateToken(token);
        }
        sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Successfully logged out\"}");
    }

    private void handleListUsers(HttpExchange exchange) throws IOException {
        List<User> users = userRepository.getAllUsers();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(users.get(i).toJson());
        }
        sb.append("]");
        sendJsonResponse(exchange, 200, sb.toString());
    }

    private String extractAuthToken(HttpExchange exchange, String query) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            return authHeader.substring(7).trim();
        }
        Map<String, String> params = parseQueryParams(query);
        return params.get("token");
    }

    private void handleListRecords(HttpExchange exchange, String query) throws IOException {
        Map<String, String> params = parseQueryParams(query);
        String q = params.get("query");
        String status = params.get("status");
        String priority = params.get("priority");
        String zone = params.get("zone");

        List<MaintenanceRecord> list = repository.search(q, status, priority, zone);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i).toJson());
        }
        sb.append("]");
        sendJsonResponse(exchange, 200, sb.toString());
    }

    private void handleGetRecord(HttpExchange exchange, String id) throws IOException {
        Optional<MaintenanceRecord> opt = repository.getById(id);
        if (opt.isPresent()) {
            sendJsonResponse(exchange, 200, opt.get().toJson());
        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Record not found: " + id + "\"}");
        }
    }

    private void handleCreateRecord(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        MaintenanceRecord r = parseRecordJson(body);

        if (r.getEquipmentId() == null || r.getEquipmentId().trim().isEmpty() ||
                r.getIssueDescription() == null || r.getIssueDescription().trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"equipmentId and issueDescription are required\"}");
            return;
        }

        MaintenanceRecord created = repository.create(r);
        sendJsonResponse(exchange, 201, created.toJson());
    }

    private void handleUpdateRecord(HttpExchange exchange, String id) throws IOException {
        String body = readBody(exchange);
        MaintenanceRecord updateData = parseRecordJson(body);

        Optional<MaintenanceRecord> updated = repository.update(id, updateData);
        if (updated.isPresent()) {
            sendJsonResponse(exchange, 200, updated.get().toJson());
        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Record not found: " + id + "\"}");
        }
    }

    private void handleDeleteRecord(HttpExchange exchange, String id) throws IOException {
        boolean ok = repository.delete(id);
        if (ok) {
            sendJsonResponse(exchange, 200, "{\"status\":\"success\",\"message\":\"Record deleted: " + id + "\"}");
        } else {
            sendJsonResponse(exchange, 404, "{\"error\":\"Record not found: " + id + "\"}");
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String text) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Minimal resilient JSON parser for MaintenanceRecord
     */
    private MaintenanceRecord parseRecordJson(String json) {
        MaintenanceRecord r = new MaintenanceRecord();
        r.setId(extractString(json, "id"));
        r.setEquipmentId(extractString(json, "equipmentId"));
        r.setEquipmentName(extractString(json, "equipmentName"));
        r.setCategory(extractString(json, "category"));
        r.setZone(extractString(json, "zone"));
        r.setIssueDescription(extractString(json, "issueDescription"));
        r.setPriority(extractString(json, "priority"));
        r.setStatus(extractString(json, "status"));
        r.setReportedBy(extractString(json, "reportedBy"));
        r.setAssignedTechnician(extractString(json, "assignedTechnician"));
        r.setResolutionNotes(extractString(json, "resolutionNotes"));

        Double cost = extractDouble(json, "estimatedCost");
        if (cost != null) r.setEstimatedCost(cost);

        return r;
    }

    private String extractString(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Double extractDouble(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
