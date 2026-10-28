package com.motosched.repository;

import com.motosched.db.Database;
import java.sql.*;

public class VehicleRepository {
    public int add(int customerId, String registration, String type, String model) throws SQLException {
        String sql = "INSERT INTO vehicles(customer_id, registration_number, vehicle_type, model) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, customerId);
            p.setString(2, registration.toUpperCase());
            p.setString(3, type.toUpperCase());
            p.setString(4, model);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) return r.getInt(1);
            }
        }
        throw new SQLException("Vehicle ID was not returned.");
    }

    public void list() throws SQLException {
        String sql = """
            SELECT v.vehicle_id, c.customer_name, v.registration_number, v.vehicle_type, v.model
            FROM vehicles v JOIN customers c ON c.customer_id = v.customer_id
            ORDER BY v.vehicle_id
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            System.out.println("\nID | Customer | Registration | Type | Model");
            while (r.next()) System.out.printf("%d | %s | %s | %s | %s%n",
                    r.getInt("vehicle_id"), r.getString("customer_name"),
                    r.getString("registration_number"), r.getString("vehicle_type"),
                    r.getString("model"));
        }
    }
}
