package com.motosched.repository;

import com.motosched.db.Database;
import java.sql.*;

public class CustomerRepository {
    public int add(String name, String phone, String email) throws SQLException {
        String sql = "INSERT INTO customers(customer_name, phone, email) VALUES(?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, email.isBlank() ? null : email);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) return r.getInt(1);
            }
        }
        throw new SQLException("Customer ID was not returned.");
    }

    public void list() throws SQLException {
        String sql = "SELECT customer_id, customer_name, phone, email FROM customers ORDER BY customer_id";
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            System.out.println("\nID | Name | Phone | Email");
            while (r.next()) System.out.printf("%d | %s | %s | %s%n",
                    r.getInt("customer_id"), r.getString("customer_name"),
                    r.getString("phone"), r.getString("email"));
        }
    }
}
