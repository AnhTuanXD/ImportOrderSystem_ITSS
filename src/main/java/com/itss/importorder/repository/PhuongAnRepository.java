package com.itss.importorder.repository;

import com.itss.importorder.entity.PhuongAnNhapHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhuongAnRepository {

    public void save(PhuongAnNhapHang phuongAn) throws SQLException {
        String sql = "INSERT INTO import_plans (plan_code, request_code, merchandise_code, created_at) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (plan_code) DO UPDATE SET request_code = EXCLUDED.request_code, " +
                     "merchandise_code = EXCLUDED.merchandise_code";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phuongAn.getPlanCode());
            pstmt.setString(2, phuongAn.getRequestCode());
            pstmt.setString(3, phuongAn.getMerchandiseCode());
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(phuongAn.getCreatedAt()));
            pstmt.executeUpdate();
        }
    }

    public PhuongAnNhapHang findByCode(String planCode) throws SQLException {
        String sql = "SELECT * FROM import_plans WHERE plan_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PhuongAnNhapHang(
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

    public List<PhuongAnNhapHang> findByRequestCode(String requestCode) throws SQLException {
        List<PhuongAnNhapHang> list = new ArrayList<>();
        String sql = "SELECT * FROM import_plans WHERE request_code = ? ORDER BY created_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PhuongAnNhapHang(
                        rs.getString("plan_code"),
                        rs.getString("request_code"),
                        rs.getString("merchandise_code"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return list;
    }

    public List<PhuongAnNhapHang> findAll() throws SQLException {
        List<PhuongAnNhapHang> list = new ArrayList<>();
        String sql = "SELECT * FROM import_plans ORDER BY created_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PhuongAnNhapHang(
                    rs.getString("plan_code"),
                    rs.getString("request_code"),
                    rs.getString("merchandise_code"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        }
        return list;
    }

    public void delete(String planCode) throws SQLException {
        String sql = "DELETE FROM import_plans WHERE plan_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            pstmt.executeUpdate();
        }
    }
}
