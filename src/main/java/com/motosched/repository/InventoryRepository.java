package com.motosched.repository;

import com.motosched.db.Database;
import java.sql.*;

public class InventoryRepository {
    public void receiveStock(int partId, int quantity) throws SQLException {
        if(quantity<=0) throw new IllegalArgumentException("Quantity must be positive.");
        String sql="UPDATE spare_parts SET quantity_in_stock=quantity_in_stock+? WHERE part_id=?";
        try(Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,quantity); p.setInt(2,partId);
            if(p.executeUpdate()==0) throw new SQLException("Part not found.");
        }
    }

    public void reserveAndConsume(int partId, int quantity) throws SQLException {
        if(quantity<=0) throw new IllegalArgumentException("Quantity must be positive.");
        try(Connection c=Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int stock;
                try(PreparedStatement p=c.prepareStatement(
                        "SELECT quantity_in_stock FROM spare_parts WHERE part_id=? FOR UPDATE")) {
                    p.setInt(1,partId);
                    try(ResultSet r=p.executeQuery()) {
                        if(!r.next()) throw new SQLException("Part not found.");
                        stock=r.getInt(1);
                    }
                }
                if(stock<quantity) throw new SQLException("Insufficient stock. Available: "+stock);
                try(PreparedStatement p=c.prepareStatement(
                        "UPDATE spare_parts SET quantity_in_stock=quantity_in_stock-? WHERE part_id=?")) {
                    p.setInt(1,quantity); p.setInt(2,partId); p.executeUpdate();
                }
                c.commit();
                System.out.println("Part stock updated successfully.");
            } catch(SQLException e) {
                c.rollback(); throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
}
