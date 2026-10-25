package com.fitness.portal.model;

import java.time.Instant;

/**
 * User represents a registered portal user with credential hashes and role permissions.
 */
public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String email;
    private String role; // STAFF, TECH, ADMIN
    private String createdAt;
    private String lastLoginAt;

    public User() {
        this.createdAt = Instant.now().toString();
        this.lastLoginAt = "";
        this.role = "STAFF";
    }

    public User(String id, String username, String passwordHash, String salt,
                String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "STAFF";
        this.createdAt = Instant.now().toString();
        this.lastLoginAt = "";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role != null ? role.toUpperCase() : "STAFF"; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    /**
     * Client-safe JSON representation (hides passwordHash and salt).
     */
    public String toJson() {
        return "{"
                + "\"id\":\"" + escapeJson(id) + "\","
                + "\"username\":\"" + escapeJson(username) + "\","
                + "\"fullName\":\"" + escapeJson(fullName) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"role\":\"" + escapeJson(role) + "\","
                + "\"createdAt\":\"" + escapeJson(createdAt) + "\","
                + "\"lastLoginAt\":\"" + escapeJson(lastLoginAt) + "\""
                + "}";
    }

    /**
     * Storage JSON representation including cryptographic hash and salt for database persistence.
     */
    public String toJsonStorage() {
        return "{"
                + "\"id\":\"" + escapeJson(id) + "\","
                + "\"username\":\"" + escapeJson(username) + "\","
                + "\"passwordHash\":\"" + escapeJson(passwordHash) + "\","
                + "\"salt\":\"" + escapeJson(salt) + "\","
                + "\"fullName\":\"" + escapeJson(fullName) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"role\":\"" + escapeJson(role) + "\","
                + "\"createdAt\":\"" + escapeJson(createdAt) + "\","
                + "\"lastLoginAt\":\"" + escapeJson(lastLoginAt) + "\""
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
