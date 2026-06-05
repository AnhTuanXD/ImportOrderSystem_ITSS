package com.itss.importorder.repository;

import com.itss.importorder.entity.PhanBo;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.PhuongThucGiaoHang;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhuongAnRepository {

    public void save(PhuongAnNhapHang phuongAn) throws SQLException {
        String sqlPlan = "INSERT INTO import_plans (plan_code, request_code, merchandise_code, created_at) " +
                         "VALUES (?, ?, ?, ?) " +
                         "ON CONFLICT (plan_code) DO UPDATE SET request_code = EXCLUDED.request_code, " +
                         "merchandise_code = EXCLUDED.merchandise_code";
        try (Connection conn = KetNoiCSDL.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlPlan)) {
                    ps.setString(1, phuongAn.getPlanCode());
                    ps.setString(2, phuongAn.getRequestCode());
                    ps.setString(3, phuongAn.getMerchandiseCode());
                    ps.setTimestamp(4, java.sql.Timestamp.valueOf(phuongAn.getCreatedAt()));
                    ps.executeUpdate();
                }

                try (PreparedStatement psDel = conn.prepareStatement(
                        "DELETE FROM plan_allocations WHERE plan_code = ?")) {
                    psDel.setString(1, phuongAn.getPlanCode());
                    psDel.executeUpdate();
                }

                String sqlAlloc = "INSERT INTO plan_allocations " +
                        "(plan_code, site_code, merchandise_code, quantity_ordered, unit, delivery_means, confirmed) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psAlloc = conn.prepareStatement(sqlAlloc)) {
                    for (PhanBo pb : phuongAn.getAllocations()) {
                        psAlloc.setString(1, phuongAn.getPlanCode());
                        psAlloc.setString(2, pb.getSiteCode());
                        psAlloc.setString(3, pb.getMerchandiseCode());
                        psAlloc.setInt(4, pb.getQuantityOrdered());
                        psAlloc.setString(5, pb.getUnit());
                        psAlloc.setString(6, pb.getPhuongThucGiaoHang().name());
                        psAlloc.setBoolean(7, pb.isConfirmed());
                        psAlloc.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public PhuongAnNhapHang findByCode(String planCode) throws SQLException {
        String sql = "SELECT ip.*, pa.site_code, pa.merchandise_code AS alloc_merch, " +
                     "pa.quantity_ordered, pa.unit AS alloc_unit, pa.delivery_means, pa.confirmed " +
                     "FROM import_plans ip " +
                     "LEFT JOIN plan_allocations pa ON ip.plan_code = pa.plan_code " +
                     "WHERE ip.plan_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                return buildFromResultSet(rs).stream().findFirst().orElse(null);
            }
        }
    }

    public List<PhuongAnNhapHang> findByRequestCode(String requestCode) throws SQLException {
        String sql = "SELECT ip.*, pa.site_code, pa.merchandise_code AS alloc_merch, " +
                     "pa.quantity_ordered, pa.unit AS alloc_unit, pa.delivery_means, pa.confirmed " +
                     "FROM import_plans ip " +
                     "LEFT JOIN plan_allocations pa ON ip.plan_code = pa.plan_code " +
                     "WHERE ip.request_code = ? ORDER BY ip.created_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                return buildFromResultSet(rs);
            }
        }
    }

    public List<PhuongAnNhapHang> findAll() throws SQLException {
        String sql = "SELECT ip.*, pa.site_code, pa.merchandise_code AS alloc_merch, " +
                     "pa.quantity_ordered, pa.unit AS alloc_unit, pa.delivery_means, pa.confirmed " +
                     "FROM import_plans ip " +
                     "LEFT JOIN plan_allocations pa ON ip.plan_code = pa.plan_code " +
                     "ORDER BY ip.created_at DESC";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return buildFromResultSet(rs);
        }
    }

    public void delete(String planCode) throws SQLException {
        String sql = "DELETE FROM import_plans WHERE plan_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            pstmt.executeUpdate();
        }
    }

    public void confirmAllocation(String planCode, String siteCode, String merchandiseCode) throws SQLException {
        String sql = "UPDATE plan_allocations SET confirmed = TRUE " +
                     "WHERE plan_code = ? AND site_code = ? AND merchandise_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, planCode);
            pstmt.setString(2, siteCode);
            pstmt.setString(3, merchandiseCode);
            pstmt.executeUpdate();
        }
    }

    private List<PhuongAnNhapHang> buildFromResultSet(ResultSet rs) throws SQLException {
        Map<String, PhuongAnNhapHang> map = new LinkedHashMap<>();
        while (rs.next()) {
            String planCode = rs.getString("plan_code");
            PhuongAnNhapHang plan = map.computeIfAbsent(planCode, k -> {
                try {
                    return new PhuongAnNhapHang(
                            planCode,
                            rs.getString("request_code"),
                            rs.getString("merchandise_code"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            String siteCode = rs.getString("site_code");
            if (siteCode != null) {
                plan.getAllocations().add(new PhanBo(
                        siteCode,
                        rs.getString("alloc_merch"),
                        rs.getInt("quantity_ordered"),
                        rs.getString("alloc_unit"),
                        PhuongThucGiaoHang.valueOf(rs.getString("delivery_means")),
                        rs.getBoolean("confirmed")));
            }
        }
        return new ArrayList<>(map.values());
    }
}
