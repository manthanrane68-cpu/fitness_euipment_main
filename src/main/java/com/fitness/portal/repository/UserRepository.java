package com.fitness.portal.repository;

import com.fitness.portal.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread-safe user authentication and credential repository with disk persistence in data/users.json.
 */
public class UserRepository {
    private static final String DATA_FILE = "data/users.json";
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1003);
    private final SecureRandom secureRandom = new SecureRandom();

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String token;
        private final User user;

        public AuthResult(boolean success, String message, String token, User user) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
        public User getUser() { return user; }
    }

    public static class UserSession {
        private final String token;
        private final String username;
        private final long expiresAtMillis;

        public UserSession(String token, String username, long expiresAtMillis) {
            this.token = token;
            this.username = username;
            this.expiresAtMillis = expiresAtMillis;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }

        public String getUsername() { return username; }
        public String getToken() { return token; }
    }

    public UserRepository() {
        initData();
    }

    public synchronized void initData() {
        usersByUsername.clear();
        usersById.clear();
        activeSessions.clear();

        // Check if data file exists on disk
        Path path = Paths.get(DATA_FILE);
        if (Files.exists(path)) {
            try {
                String content = Files.readString(path, StandardCharsets.UTF_8);
                List<User> loaded = parseUsersJson(content);
                if (!loaded.isEmpty()) {
                    for (User u : loaded) {
                        usersByUsername.put(u.getUsername().toLowerCase(), u);
                        usersById.put(u.getId(), u);
                    }
                    return;
                }
            } catch (Exception e) {
                System.err.println("Notice: Could not read existing users from disk: " + e.getMessage());
            }
        }

        // Seed default portal accounts for each role
        createSeedUser("USR-1001", "admin", "admin123", "Elena Rostova (Facility Admin)",
                "admin@pulsefit.com", "ADMIN");
        createSeedUser("USR-1002", "technician", "tech123", "Dave Miller (Certified Tech)",
                "dave.miller@pulsefit.com", "TECH");
        createSeedUser("USR-1003", "trainer", "staff123", "Marcus Vance (Floor Trainer)",
                "marcus.vance@pulsefit.com", "STAFF");

        saveToDisk();
    }

    private void createSeedUser(String id, String username, String rawPassword,
                                String fullName, String email, String role) {
        String salt = generateSalt();
        String hash = hashPassword(rawPassword, salt);
        User user = new User(id, username, hash, salt, fullName, email, role);
        usersByUsername.put(username.toLowerCase(), user);
        usersById.put(id, user);
    }

    public synchronized AuthResult authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return new AuthResult(false, "Username and password are required", null, null);
        }

        User user = usersByUsername.get(username.trim().toLowerCase());
        if (user == null) {
            return new AuthResult(false, "Invalid username or password", null, null);
        }

        String expectedHash = hashPassword(rawPassword, user.getSalt());
        if (!expectedHash.equals(user.getPasswordHash())) {
            return new AuthResult(false, "Invalid username or password", null, null);
        }

        // Update last login
        user.setLastLoginAt(Instant.now().toString());
        saveToDisk();

        // Create 24h active session
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + (24L * 60 * 60 * 1000);
        activeSessions.put(token, new UserSession(token, user.getUsername(), expiresAt));

        return new AuthResult(true, "Authentication successful", token, user);
    }

    public synchronized AuthResult register(String username, String rawPassword,
                                           String fullName, String email, String role) {
        if (username == null || username.trim().length() < 3) {
            return new AuthResult(false, "Username must be at least 3 characters", null, null);
        }
        if (rawPassword == null || rawPassword.trim().length() < 5) {
            return new AuthResult(false, "Password must be at least 5 characters", null, null);
        }

        String cleanUsername = username.trim();
        if (usersByUsername.containsKey(cleanUsername.toLowerCase())) {
            return new AuthResult(false, "Username '" + cleanUsername + "' is already registered", null, null);
        }

        String id = "USR-" + idCounter.incrementAndGet();
        String salt = generateSalt();
        String hash = hashPassword(rawPassword, salt);
        String cleanRole = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "STAFF";
        if (!cleanRole.equals("ADMIN") && !cleanRole.equals("TECH") && !cleanRole.equals("STAFF")) {
            cleanRole = "STAFF";
        }

        String cleanFullName = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : cleanUsername;
        String cleanEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : (cleanUsername.toLowerCase() + "@pulsefit.com");

        User user = new User(id, cleanUsername, hash, salt, cleanFullName, cleanEmail, cleanRole);
        user.setLastLoginAt(Instant.now().toString());

        usersByUsername.put(cleanUsername.toLowerCase(), user);
        usersById.put(id, user);
        saveToDisk();

        // Automatically create session
        String token = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + (24L * 60 * 60 * 1000);
        activeSessions.put(token, new UserSession(token, user.getUsername(), expiresAt));

        return new AuthResult(true, "User registered successfully", token, user);
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) return Optional.empty();
        UserSession session = activeSessions.get(token.trim());
        if (session == null || session.isExpired()) {
            if (session != null) activeSessions.remove(token.trim());
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(session.getUsername().toLowerCase()));
    }

    public boolean invalidateToken(String token) {
        if (token == null) return false;
        return activeSessions.remove(token.trim()) != null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>(usersByUsername.values());
        list.sort(Comparator.comparing(User::getUsername));
        return list;
    }

    public Optional<User> getUserByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(username.trim().toLowerCase()));
    }

    public synchronized void reset() {
        usersByUsername.clear();
        usersById.clear();
        activeSessions.clear();
        createSeedUser("USR-1001", "admin", "admin123", "Elena Rostova (Facility Admin)",
                "admin@pulsefit.com", "ADMIN");
        createSeedUser("USR-1002", "technician", "tech123", "Dave Miller (Certified Tech)",
                "dave.miller@pulsefit.com", "TECH");
        createSeedUser("USR-1003", "trainer", "staff123", "Marcus Vance (Floor Trainer)",
                "marcus.vance@pulsefit.com", "STAFF");
        saveToDisk();
    }

    private synchronized void saveToDisk() {
        try {
            Files.createDirectories(Paths.get("data"));
            StringBuilder sb = new StringBuilder("[\n");
            int count = 0;
            for (User u : usersByUsername.values()) {
                if (count++ > 0) sb.append(",\n");
                sb.append("  ").append(u.toJsonStorage());
            }
            sb.append("\n]");
            Files.writeString(Paths.get(DATA_FILE), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Notice: Could not persist users to disk: " + e.getMessage());
        }
    }

    private String generateSalt() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    private List<User> parseUsersJson(String json) {
        List<User> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        Pattern objPattern = Pattern.compile("\\{[^{}]*\\}");
        Matcher objMatcher = objPattern.matcher(json);

        while (objMatcher.find()) {
            String block = objMatcher.group();
            String id = extractString(block, "id");
            String username = extractString(block, "username");
            String passwordHash = extractString(block, "passwordHash");
            String salt = extractString(block, "salt");
            String fullName = extractString(block, "fullName");
            String email = extractString(block, "email");
            String role = extractString(block, "role");
            String createdAt = extractString(block, "createdAt");
            String lastLoginAt = extractString(block, "lastLoginAt");

            if (username != null && passwordHash != null && salt != null) {
                User user = new User(id != null ? id : "USR-1000", username, passwordHash, salt,
                        fullName != null ? fullName : username,
                        email != null ? email : "",
                        role != null ? role : "STAFF");
                if (createdAt != null) user.setCreatedAt(createdAt);
                if (lastLoginAt != null) user.setLastLoginAt(lastLoginAt);
                list.add(user);
            }
        }
        return list;
    }

    private String extractString(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
