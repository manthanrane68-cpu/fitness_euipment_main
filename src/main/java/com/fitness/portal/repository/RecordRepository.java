package com.fitness.portal.repository;

import com.fitness.portal.model.DashboardStats;
import com.fitness.portal.model.MaintenanceRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory and disk-persisted repository for maintenance records.
 */
public class RecordRepository {
    private static final String DATA_FILE = "data/maintenance_records.json";
    private final Map<String, MaintenanceRecord> records = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1005);

    public RecordRepository() {
        initData();
    }

    private synchronized void initData() {
        records.clear();
        // Seed default realistic equipment records across zones
        addSeed(new MaintenanceRecord("TKT-1001", "TM-01", "Matrix Commercial Treadmill T7xe",
                "Cardio", "Zone A - Cardio Deck",
                "Drive belt slipping at high speeds (>10 km/h); motor heat error E-02",
                "High", "In Repair", "Marcus Vance (Floor Trainer)", "Dave Miller (OEM Certified)",
                "Ordered replacement drive belt; recalibrated roller tension.", 185.00));

        addSeed(new MaintenanceRecord("TKT-1002", "CB-04", "LifeFitness Dual Adjustable Pulley",
                "Strength", "Zone B - Strength Zone",
                "Left high-pulley steel cable fraying near selector pin; urgent safety issue",
                "Critical", "Reported", "Sarah Jenkins (Head Trainer)", "Unassigned",
                "", 0.00));

        addSeed(new MaintenanceRecord("TKT-1003", "EB-02", "Technogym Excite Synchro Elliptical",
                "Cardio", "Zone A - Cardio Deck",
                "Right footplate squeaking and resistance fluctuates unexpectedly",
                "Medium", "Awaiting Parts", "Kevin Brown (Front Desk)", "Alex Rivera (Technician)",
                "Alternator belt worn out; waiting for OEM shipment from distributor.", 120.00));

        addSeed(new MaintenanceRecord("TKT-1004", "SM-01", "Hammer Strength Smith Machine",
                "Strength", "Zone B - Strength Zone",
                "Linear bearings catching on vertical guide rod at bottom 30cm",
                "High", "Resolved", "Marcus Vance (Floor Trainer)", "Dave Miller (OEM Certified)",
                "Stripped, cleaned guide rails, and replaced linear ball bearings.", 240.00));

        addSeed(new MaintenanceRecord("TKT-1005", "RB-03", "Concept2 RowErg Model D",
                "Cardio", "Zone C - Functional Zone",
                "PM5 performance monitor display flickering and failing to read Bluetooth HR",
                "Low", "Closed", "Elena Rostova (Coach)", "In-House Tech Team",
                "Replaced battery contacts and updated PM5 firmware to v32.", 45.00));

        saveToDisk();
    }

    private void addSeed(MaintenanceRecord r) {
        records.put(r.getId(), r);
    }

    public List<MaintenanceRecord> getAll() {
        List<MaintenanceRecord> list = new ArrayList<>(records.values());
        list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return list;
    }

    public Optional<MaintenanceRecord> getById(String id) {
        return Optional.ofNullable(records.get(id));
    }

    public synchronized MaintenanceRecord create(MaintenanceRecord record) {
        if (record.getId() == null || record.getId().trim().isEmpty()) {
            record.setId("TKT-" + idCounter.incrementAndGet());
        }
        record.setCreatedAt(Instant.now().toString());
        record.setUpdatedAt(Instant.now().toString());
        if (record.getStatus() == null || record.getStatus().trim().isEmpty()) {
            record.setStatus("Reported");
        }
        records.put(record.getId(), record);
        saveToDisk();
        return record;
    }

    public synchronized Optional<MaintenanceRecord> update(String id, MaintenanceRecord updateData) {
        MaintenanceRecord existing = records.get(id);
        if (existing == null) {
            return Optional.empty();
        }

        if (updateData.getEquipmentId() != null) existing.setEquipmentId(updateData.getEquipmentId());
        if (updateData.getEquipmentName() != null) existing.setEquipmentName(updateData.getEquipmentName());
        if (updateData.getCategory() != null) existing.setCategory(updateData.getCategory());
        if (updateData.getZone() != null) existing.setZone(updateData.getZone());
        if (updateData.getIssueDescription() != null) existing.setIssueDescription(updateData.getIssueDescription());
        if (updateData.getPriority() != null) existing.setPriority(updateData.getPriority());
        if (updateData.getStatus() != null) existing.setStatus(updateData.getStatus());
        if (updateData.getReportedBy() != null) existing.setReportedBy(updateData.getReportedBy());
        if (updateData.getAssignedTechnician() != null) existing.setAssignedTechnician(updateData.getAssignedTechnician());
        if (updateData.getResolutionNotes() != null) existing.setResolutionNotes(updateData.getResolutionNotes());
        if (updateData.getEstimatedCost() >= 0) existing.setEstimatedCost(updateData.getEstimatedCost());

        existing.setUpdatedAt(Instant.now().toString());
        records.put(id, existing);
        saveToDisk();
        return Optional.of(existing);
    }

    public synchronized boolean delete(String id) {
        MaintenanceRecord removed = records.remove(id);
        if (removed != null) {
            saveToDisk();
            return true;
        }
        return false;
    }

    public List<MaintenanceRecord> search(String query, String status, String priority, String zone) {
        List<MaintenanceRecord> list = new ArrayList<>(records.values());
        String q = query != null ? query.trim().toLowerCase() : "";
        String st = status != null ? status.trim() : "";
        String pr = priority != null ? priority.trim() : "";
        String zn = zone != null ? zone.trim() : "";

        return list.stream()
                .filter(r -> {
                    if (!q.isEmpty()) {
                        boolean matchId = r.getId().toLowerCase().contains(q);
                        boolean matchEqId = r.getEquipmentId() != null && r.getEquipmentId().toLowerCase().contains(q);
                        boolean matchEqName = r.getEquipmentName() != null && r.getEquipmentName().toLowerCase().contains(q);
                        boolean matchIssue = r.getIssueDescription() != null && r.getIssueDescription().toLowerCase().contains(q);
                        if (!matchId && !matchEqId && !matchEqName && !matchIssue) {
                            return false;
                        }
                    }
                    if (!st.isEmpty() && !st.equalsIgnoreCase("ALL") && !r.getStatus().equalsIgnoreCase(st)) {
                        return false;
                    }
                    if (!pr.isEmpty() && !pr.equalsIgnoreCase("ALL") && !r.getPriority().equalsIgnoreCase(pr)) {
                        return false;
                    }
                    if (!zn.isEmpty() && !zn.equalsIgnoreCase("ALL") && !r.getZone().equalsIgnoreCase(zn)) {
                        return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    public DashboardStats getDashboardStats() {
        int total = records.size();
        int open = 0;
        int inProgress = 0;
        int resolved = 0;
        int critical = 0;
        double cost = 0.0;
        Map<String, Integer> categoryCounts = new HashMap<>();

        for (MaintenanceRecord r : records.values()) {
            String s = r.getStatus();
            if ("Reported".equalsIgnoreCase(s) || "Triaged".equalsIgnoreCase(s)) {
                open++;
            } else if ("In Repair".equalsIgnoreCase(s) || "Awaiting Parts".equalsIgnoreCase(s)) {
                inProgress++;
            } else if ("Resolved".equalsIgnoreCase(s) || "Closed".equalsIgnoreCase(s)) {
                resolved++;
            }

            if ("Critical".equalsIgnoreCase(r.getPriority())) {
                critical++;
            }

            cost += r.getEstimatedCost();

            String cat = r.getCategory() != null ? r.getCategory() : "Other";
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
        }

        return new DashboardStats(total, open, inProgress, resolved, critical, cost, categoryCounts);
    }

    public synchronized void reset() {
        initData();
    }

    private void saveToDisk() {
        try {
            Files.createDirectories(Paths.get("data"));
            StringBuilder sb = new StringBuilder("[\n");
            int count = 0;
            for (MaintenanceRecord r : records.values()) {
                if (count++ > 0) sb.append(",\n");
                sb.append("  ").append(r.toJson());
            }
            sb.append("\n]");
            Files.writeString(Paths.get(DATA_FILE), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Notice: Could not persist records to disk: " + e.getMessage());
        }
    }
}
