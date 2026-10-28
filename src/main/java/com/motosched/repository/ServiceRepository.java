package com.motosched.repository;

import com.motosched.db.Database;
import com.motosched.model.ServiceJob;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository {
    public int add(int vehicleId, String serviceType, int duration, int priority) throws SQLException {
        String sql = """
            INSERT INTO service_requests(vehicle_id, service_type, arrival_time,
                estimated_duration, priority, status)
            VALUES(?, ?, CURRENT_TIMESTAMP, ?, ?, 'READY')
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, vehicleId);
            p.setString(2, serviceType);
            p.setInt(3, duration);
            p.setInt(4, priority);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) return r.getInt(1);
            }
        }
        throw new SQLException("Service ID was not returned.");
    }

    public List<ServiceJob> findReadyJobs() throws SQLException {
        List<ServiceJob> jobs = new ArrayList<>();
        String sql = """
            SELECT sr.service_id, v.registration_number, sr.service_type,
                   sr.arrival_time, sr.estimated_duration, sr.priority
            FROM service_requests sr
            JOIN vehicles v ON v.vehicle_id = sr.vehicle_id
            WHERE sr.status = 'READY'
            ORDER BY sr.arrival_time, sr.service_id
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            int order = 0;
            while (r.next()) {
                jobs.add(new ServiceJob(r.getInt("service_id"),
                        r.getString("registration_number"),
                        r.getString("service_type"), order++,
                        r.getInt("estimated_duration"), r.getInt("priority")));
            }
        }
        return jobs;
    }

    public void listAll() throws SQLException {
        String sql = """
            SELECT sr.service_id, v.registration_number, sr.service_type,
                   sr.arrival_time, sr.estimated_duration, sr.priority, sr.status
            FROM service_requests sr JOIN vehicles v ON v.vehicle_id = sr.vehicle_id
            ORDER BY sr.arrival_time, sr.service_id
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            System.out.println("\nID | Vehicle | Service | Arrival | Minutes | Priority | Status");
            while (r.next()) System.out.printf("%d | %s | %s | %s | %d | %d | %s%n",
                    r.getInt("service_id"), r.getString("registration_number"),
                    r.getString("service_type"), r.getTimestamp("arrival_time"),
                    r.getInt("estimated_duration"), r.getInt("priority"), r.getString("status"));
        }
    }

    public void updateStatus(int serviceId, String status) throws SQLException {
        String sql = "UPDATE service_requests SET status=? WHERE service_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, status);
            p.setInt(2, serviceId);
            if (p.executeUpdate() == 0) throw new SQLException("Service request not found.");
        }
    }

    public void complete(int serviceId, int actualDuration) throws SQLException {
        String sql = "UPDATE service_requests SET status='COMPLETED', actual_duration=? WHERE service_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, actualDuration);
            p.setInt(2, serviceId);
            if (p.executeUpdate() == 0) throw new SQLException("Service request not found.");
        }
    }

    public void showHistory(String registration) throws SQLException {
        String sql = """
            SELECT sr.service_id, sr.service_type, sr.arrival_time,
                   sr.status, sr.estimated_duration, sr.actual_duration
            FROM service_requests sr
            JOIN vehicles v ON v.vehicle_id=sr.vehicle_id
            WHERE v.registration_number=?
            ORDER BY sr.arrival_time DESC
            """;
        try (Connection c = Database.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, registration.toUpperCase());
            try (ResultSet r = p.executeQuery()) {
                System.out.println("\nID | Service | Arrival | Status | Estimate | Actual");
                while (r.next()) System.out.printf("%d | %s | %s | %s | %d | %s%n",
                        r.getInt("service_id"), r.getString("service_type"),
                        r.getTimestamp("arrival_time"), r.getString("status"),
                        r.getInt("estimated_duration"),
                        r.getObject("actual_duration") == null ? "-" : r.getInt("actual_duration"));
            }
        }
    }
}
