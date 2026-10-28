package com.motosched.repository;

import com.motosched.db.Database;
import java.sql.*;

public class WorkshopRepository {
    public int addMechanic(String name, String skill) throws SQLException {
        String sql = "INSERT INTO mechanics(mechanic_name, specialization) VALUES(?,?)";
        try (Connection c=Database.getConnection();
             PreparedStatement p=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1,name); p.setString(2,skill); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){ if(r.next()) return r.getInt(1); }
        }
        throw new SQLException("Mechanic ID not returned.");
    }

    public int addBay(String type) throws SQLException {
        String sql = "INSERT INTO service_bays(bay_type) VALUES(?)";
        try (Connection c=Database.getConnection();
             PreparedStatement p=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1,type.toUpperCase()); p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){ if(r.next()) return r.getInt(1); }
        }
        throw new SQLException("Bay ID not returned.");
    }

    public void addPart(String name, int quantity, double price) throws SQLException {
        String sql = "INSERT INTO spare_parts(part_name, quantity_in_stock, unit_price) VALUES(?,?,?)";
        try (Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,name); p.setInt(2,quantity); p.setDouble(3,price); p.executeUpdate();
        }
    }

    public void listResources() throws SQLException {
        try (Connection c=Database.getConnection()) {
            try (PreparedStatement p=c.prepareStatement(
                    "SELECT mechanic_id, mechanic_name, specialization, is_active FROM mechanics ORDER BY mechanic_id");
                 ResultSet r=p.executeQuery()) {
                System.out.println("\nMECHANICS: ID | Name | Skill | Active");
                while(r.next()) System.out.printf("%d | %s | %s | %s%n",
                        r.getInt(1),r.getString(2),r.getString(3),r.getBoolean(4));
            }
            try (PreparedStatement p=c.prepareStatement(
                    "SELECT bay_id, bay_type, is_active FROM service_bays ORDER BY bay_id");
                 ResultSet r=p.executeQuery()) {
                System.out.println("\nBAYS: ID | Type | Active");
                while(r.next()) System.out.printf("%d | %s | %s%n",
                        r.getInt(1),r.getString(2),r.getBoolean(3));
            }
            try (PreparedStatement p=c.prepareStatement(
                    "SELECT part_id, part_name, quantity_in_stock, unit_price FROM spare_parts ORDER BY part_id");
                 ResultSet r=p.executeQuery()) {
                System.out.println("\nPARTS: ID | Name | Stock | Price");
                while(r.next()) System.out.printf("%d | %s | %d | %.2f%n",
                        r.getInt(1),r.getString(2),r.getInt(3),r.getDouble(4));
            }
        }
    }

    public void assignService(int serviceId, int mechanicId, int bayId) throws SQLException {
        try (Connection c=Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Lock mechanic and bay rows to reduce concurrent double allocation.
                try (PreparedStatement p=c.prepareStatement(
                        "SELECT is_active FROM mechanics WHERE mechanic_id=? FOR UPDATE")) {
                    p.setInt(1,mechanicId);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next() || !r.getBoolean(1)) throw new SQLException("Mechanic missing or inactive.");
                    }
                }
                try (PreparedStatement p=c.prepareStatement(
                        "SELECT is_active FROM service_bays WHERE bay_id=? FOR UPDATE")) {
                    p.setInt(1,bayId);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next() || !r.getBoolean(1)) throw new SQLException("Bay missing or inactive.");
                    }
                }
                try (PreparedStatement p=c.prepareStatement(
                        "SELECT COUNT(*) FROM service_assignments WHERE assignment_status='ACTIVE' AND (mechanic_id=? OR bay_id=?)")) {
                    p.setInt(1,mechanicId); p.setInt(2,bayId);
                    try(ResultSet r=p.executeQuery()) {
                        r.next();
                        if(r.getInt(1)>0) throw new SQLException("Mechanic or bay is already assigned to an active job.");
                    }
                }
                try (PreparedStatement p=c.prepareStatement(
                        "INSERT INTO service_assignments(service_id,mechanic_id,bay_id) VALUES(?,?,?)")) {
                    p.setInt(1,serviceId); p.setInt(2,mechanicId); p.setInt(3,bayId); p.executeUpdate();
                }
                try (PreparedStatement p=c.prepareStatement(
                        "UPDATE service_requests SET status='RUNNING' WHERE service_id=? AND status='READY'")) {
                    p.setInt(1,serviceId);
                    if(p.executeUpdate()==0) throw new SQLException("Service must exist and be READY.");
                }
                c.commit();
                System.out.println("Resources assigned; service marked RUNNING.");
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void releaseService(int serviceId) throws SQLException {
        String sql = """
            UPDATE service_assignments SET assignment_status='COMPLETED', end_time=CURRENT_TIMESTAMP
            WHERE service_id=? AND assignment_status='ACTIVE'
            """;
        try(Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,serviceId);
            int count=p.executeUpdate();
            if(count==0) System.out.println("No active assignment found for this service.");
            else System.out.println("Active mechanic/bay assignment released.");
        }
    }
}
