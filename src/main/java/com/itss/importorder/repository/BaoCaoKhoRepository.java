package com.itss.importorder.repository;

import com.itss.importorder.entity.BaoCaoKho;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BaoCaoKhoRepository {

    public void save(BaoCaoKho baoCao) throws SQLException {
        String sql = "INSERT INTO warehouse_reports (report_code, request_code, checker, checked_at, result, note) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (report_code) DO UPDATE SET request_code = EXCLUDED.request_code, " +
                     "checker = EXCLUDED.checker, checked_at = EXCLUDED.checked_at, " +
                     "result = EXCLUDED.result, note = EXCLUDED.note";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, baoCao.getReportCode());
            pstmt.setString(2, baoCao.getRequestCode());
            pstmt.setString(3, baoCao.getChecker());
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(baoCao.getCheckedAt()));
            pstmt.setString(5, baoCao.getResult());
            pstmt.setString(6, baoCao.getNote());
            pstmt.executeUpdate();
        }
    }

    public BaoCaoKho findByCode(String reportCode) throws SQLException {
        String sql = "SELECT * FROM warehouse_reports WHERE report_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reportCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildBaoCao(rs);
                }
            }
        }
        return null;
    }

    public List<BaoCaoKho> findByRequestCode(String requestCode) throws SQLException {
        List<BaoCaoKho> list = new ArrayList<>();
        String sql = "SELECT * FROM warehouse_reports WHERE request_code = ? ORDER BY checked_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(buildBaoCao(rs));
                }
            }
        }
        return list;
    }

    public List<BaoCaoKho> findAll() throws SQLException {
        List<BaoCaoKho> list = new ArrayList<>();
        String sql = "SELECT * FROM warehouse_reports ORDER BY checked_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(buildBaoCao(rs));
            }
        }
        return list;
    }

    public void delete(String reportCode) throws SQLException {
        String sql = "DELETE FROM warehouse_reports WHERE report_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reportCode);
            pstmt.executeUpdate();
        }
    }

    private BaoCaoKho buildBaoCao(ResultSet rs) throws SQLException {
        return new BaoCaoKho(
            rs.getString("report_code"),
            rs.getString("request_code"),
            rs.getString("checker"),
            rs.getTimestamp("checked_at").toLocalDateTime(),
            rs.getString("result"),
            rs.getString("note")
        );
    }
}
