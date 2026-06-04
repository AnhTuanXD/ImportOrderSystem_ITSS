package com.itss.importorder.database;

import com.itss.importorder.model.ImportPlan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportPlanRepository {
    
    public void save(ImportPlan plan) throws SQLException {
        String sql = "INSERT INTO import_plans (plan_code, request_code, merchandise_code, created_at) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (plan_code) DO UPDATE SET request_code = EXCLUDED.request_code, " +
                     "merchandise_code = EXCLUDED.merchandise_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plan.getPlanCode());
            pstmt.setString(2, plan.getRequestCode());
            pstmt.setString(3, plan.getMerchandiseCode());
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(plan.getCreatedAt()));
            pstmt.executeUpdate();
        }
    }
    
    public ImportPlan findByCode(String planCode) throws SQLException {
        String sql = "SELECT * FROM import_plans WHERE plan_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ImportPlan(
                        rs.getString("plan_code"),
                        rs.getString("request_code"),
                        rs.getString("merchandise_code"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }
        }
        return null;
    }
    
    public List<ImportPlan> findByRequestCode(String requestCode) throws SQLException {
        List<ImportPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM import_plans WHERE request_code = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    plans.add(new ImportPlan(
                        rs.getString("plan_code"),
                        rs.getString("request_code"),
                        rs.getString("merchandise_code"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return plans;
    }
    
    public List<ImportPlan> findAll() throws SQLException {
        List<ImportPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM import_plans ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                plans.add(new ImportPlan(
                    rs.getString("plan_code"),
                    rs.getString("request_code"),
                    rs.getString("merchandise_code"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return plans;
    }
    
    public void delete(String planCode) throws SQLException {
        String sql = "DELETE FROM import_plans WHERE plan_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            pstmt.executeUpdate();
        }
    }
}
