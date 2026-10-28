package com.motosched.repository;

import com.motosched.db.Database;
import java.sql.*;

public class InvoiceRepository {
    public void create(int serviceId, double laborCost, double partsCost, double tax) throws SQLException {
        if(laborCost<0 || partsCost<0 || tax<0) throw new IllegalArgumentException("Costs cannot be negative.");
        double total=laborCost+partsCost+tax;
        String sql="INSERT INTO invoices(service_id,labor_cost,parts_cost,tax,total_amount) VALUES(?,?,?,?,?)";
        try(Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,serviceId); p.setDouble(2,laborCost); p.setDouble(3,partsCost);
            p.setDouble(4,tax); p.setDouble(5,total); p.executeUpdate();
            System.out.printf("Invoice created. Total = %.2f%n",total);
        }
    }

    public void list() throws SQLException {
        String sql="SELECT invoice_id,service_id,labor_cost,parts_cost,tax,total_amount,generated_at FROM invoices ORDER BY invoice_id";
        try(Connection c=Database.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            System.out.println("\nInvoice | Service | Labor | Parts | Tax | Total | Generated");
            while(r.next()) System.out.printf("%d | %d | %.2f | %.2f | %.2f | %.2f | %s%n",
                    r.getInt(1),r.getInt(2),r.getDouble(3),r.getDouble(4),r.getDouble(5),r.getDouble(6),r.getTimestamp(7));
        }
    }
}
