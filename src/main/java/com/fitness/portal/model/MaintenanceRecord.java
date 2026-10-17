package com.fitness.portal.model;

import java.time.Instant;

/**
 * MaintenanceRecord represents a single equipment breakdown or servicing ticket.
 */
public class MaintenanceRecord {
    private String id;
    private String equipmentId;
    private String equipmentName;
    private String category; // Cardio, Strength, Free Weights, Functional
    private String zone;     // Zone A - Cardio, Zone B - Strength, Zone C - Functional, Zone D - Recovery
    private String issueDescription;
    private String priority; // Low, Medium, High, Critical
    private String status;   // Reported, Triaged, In Repair, Awaiting Parts, Resolved, Closed
    private String reportedBy;
    private String assignedTechnician;
    private String resolutionNotes;
    private double estimatedCost;
    private String createdAt;
    private String updatedAt;

    public MaintenanceRecord() {
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
        this.status = "Reported";
        this.priority = "Medium";
        this.estimatedCost = 0.0;
        this.assignedTechnician = "Unassigned";
        this.resolutionNotes = "";
    }

    public MaintenanceRecord(String id, String equipmentId, String equipmentName, String category,
                             String zone, String issueDescription, String priority, String status,
                             String reportedBy, String assignedTechnician, String resolutionNotes,
                             double estimatedCost) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.category = category;
        this.zone = zone;
        this.issueDescription = issueDescription;
        this.priority = priority;
        this.status = status;
        this.reportedBy = reportedBy;
        this.assignedTechnician = assignedTechnician;
        this.resolutionNotes = resolutionNotes;
        this.estimatedCost = estimatedCost;
        this.createdAt = Instant.now().toString();
        this.updatedAt = Instant.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public String getAssignedTechnician() { return assignedTechnician; }
    public void setAssignedTechnician(String assignedTechnician) { this.assignedTechnician = assignedTechnician; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Converts the entity to a clean JSON string.
     */
    public String toJson() {
        return "{"
                + "\"id\":\"" + escapeJson(id) + "\","
                + "\"equipmentId\":\"" + escapeJson(equipmentId) + "\","
                + "\"equipmentName\":\"" + escapeJson(equipmentName) + "\","
                + "\"category\":\"" + escapeJson(category) + "\","
                + "\"zone\":\"" + escapeJson(zone) + "\","
                + "\"issueDescription\":\"" + escapeJson(issueDescription) + "\","
                + "\"priority\":\"" + escapeJson(priority) + "\","
                + "\"status\":\"" + escapeJson(status) + "\","
                + "\"reportedBy\":\"" + escapeJson(reportedBy) + "\","
                + "\"assignedTechnician\":\"" + escapeJson(assignedTechnician) + "\","
                + "\"resolutionNotes\":\"" + escapeJson(resolutionNotes) + "\","
                + "\"estimatedCost\":" + estimatedCost + ","
                + "\"createdAt\":\"" + escapeJson(createdAt) + "\","
                + "\"updatedAt\":\"" + escapeJson(updatedAt) + "\""
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
