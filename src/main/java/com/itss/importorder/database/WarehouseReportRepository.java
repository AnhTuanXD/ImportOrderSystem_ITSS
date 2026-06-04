package com.itss.importorder.database;

import com.itss.importorder.model.WarehouseReport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseReportRepository {
    
    public void save(WarehouseReport report) throws SQLException {
        String sql = "INSERT INTO warehouse_reports (report_code, request_code, checker, checked_at, result, note) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (report_code) DO UPDATE SET request_code = EXCLUDED.request_code, " +
                     "checker = EXCLUDED.checker, checked_at = EXCLUDED.checked_at, " +
                     "result = EXCLUDED.result, note = EXCLUDED.note";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, report.getReportCode());
            pstmt.setString(2, report.getRequestCode());
            pstmt.setString(3, report.getChecker());
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(report.getCheckedAt()));
            pstmt.setString(5, report.getResult());
            pstmt.setString(6, report.getNote());
            pstmt.executeUpdate();
        }
    }
    
    public WarehouseReport findByCode(String reportCode) throws SQLException {
        String sql = "SELECT * FROM warehouse_reports WHERE report_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reportCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new WarehouseReport(
                        rs.getString("report_code"),
                        rs.getString("request_code"),
                        rs.getString("checker"),
                        rs.getTimestamp("checked_at").toLocalDateTime(),
                        rs.getString("result"),
                        rs.getString("note")
                    );
                }
            }
        }
        return null;
    }
    
    public List<WarehouseReport> findByRequestCode(String requestCode) throws SQLException {
        List<WarehouseReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM warehouse_reports WHERE request_code = ? ORDER BY checked_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(new WarehouseReport(
                        rs.getString("report_code"),
                        rs.getString("request_code"),
                        rs.getString("checker"),
                        rs.getTimestamp("checked_at").toLocalDateTime(),
                        rs.getString("result"),
                        rs.getString("note")
                    ));
                }
            }
        }
        return reports;
    }
    
    public List<WarehouseReport> findAll() throws SQLException {
        List<WarehouseReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM warehouse_reports ORDER BY checked_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(new WarehouseReport(
                    rs.getString("report_code"),
                    rs.getString("request_code"),
                    rs.getString("checker"),
                    rs.getTimestamp("checked_at").toLocalDateTime(),
                    rs.getString("result"),
                    rs.getString("note")
                ));
            }
        }
        return reports;
    }
    
    public void delete(String reportCode) throws SQLException {
        String sql = "DELETE FROM warehouse_reports WHERE report_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reportCode);
            pstmt.executeUpdate();
        }
    }
}
