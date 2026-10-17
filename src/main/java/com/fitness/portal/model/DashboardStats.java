package com.fitness.portal.model;

import java.util.Map;

/**
 * Aggregated KPI metrics for the executive dashboard.
 */
public class DashboardStats {
    private int totalRecords;
    private int openIssues;
    private int inProgress;
    private int resolved;
    private int criticalCount;
    private double totalCost;
    private Map<String, Integer> categoryBreakdown;

    public DashboardStats(int totalRecords, int openIssues, int inProgress, int resolved,
                          int criticalCount, double totalCost, Map<String, Integer> categoryBreakdown) {
        this.totalRecords = totalRecords;
        this.openIssues = openIssues;
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.criticalCount = criticalCount;
        this.totalCost = totalCost;
        this.categoryBreakdown = categoryBreakdown;
    }

    public int getTotalRecords() { return totalRecords; }
    public int getOpenIssues() { return openIssues; }
    public int getInProgress() { return inProgress; }
    public int getResolved() { return resolved; }
    public int getCriticalCount() { return criticalCount; }
    public double getTotalCost() { return totalCost; }
    public Map<String, Integer> getCategoryBreakdown() { return categoryBreakdown; }

    public String toJson() {
        StringBuilder catJson = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Integer> entry : categoryBreakdown.entrySet()) {
            if (i++ > 0) catJson.append(",");
            catJson.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
        }
        catJson.append("}");

        return "{"
                + "\"totalRecords\":" + totalRecords + ","
                + "\"openIssues\":" + openIssues + ","
                + "\"inProgress\":" + inProgress + ","
                + "\"resolved\":" + resolved + ","
                + "\"criticalCount\":" + criticalCount + ","
                + "\"totalCost\":" + String.format(java.util.Locale.US, "%.2f", totalCost) + ","
                + "\"telemetryExportFormat\":\"JSON_V1_STABLE\","
                + "\"categoryBreakdown\":" + catJson.toString()
                + "}";
    }
}
