package com.itss.importorder.database;

import com.itss.importorder.model.StockRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockRecordRepository {
    
    public void save(StockRecord record) throws SQLException {
        String sql = "INSERT INTO stock_records (site_code, merchandise_code, in_stock_quantity, unit) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (site_code, merchandise_code) DO UPDATE SET " +
                     "in_stock_quantity = EXCLUDED.in_stock_quantity, unit = EXCLUDED.unit";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, record.getSiteCode());
            pstmt.setString(2, record.getMerchandiseCode());
            pstmt.setInt(3, record.getInStockQuantity());
            pstmt.setString(4, record.getUnit());
            pstmt.executeUpdate();
        }
    }
    
    public List<StockRecord> findBySiteCode(String siteCode) throws SQLException {
        List<StockRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM stock_records WHERE site_code = ? ORDER BY merchandise_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new StockRecord(
                        rs.getString("site_code"),
                        rs.getString("merchandise_code"),
                        rs.getInt("in_stock_quantity"),
                        rs.getString("unit")
                    ));
                }
            }
        }
        return records;
    }
    
    public List<StockRecord> findAll() throws SQLException {
        List<StockRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM stock_records ORDER BY site_code, merchandise_code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(new StockRecord(
                    rs.getString("site_code"),
                    rs.getString("merchandise_code"),
                    rs.getInt("in_stock_quantity"),
                    rs.getString("unit")
                ));
            }
        }
        return records;
    }
    
    public void delete(String siteCode, String merchandiseCode) throws SQLException {
        String sql = "DELETE FROM stock_records WHERE site_code = ? AND merchandise_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            pstmt.setString(2, merchandiseCode);
            pstmt.executeUpdate();
        }
    }
}
