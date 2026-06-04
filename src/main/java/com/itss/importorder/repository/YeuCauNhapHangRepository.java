package com.itss.importorder.repository;

import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.TrangThaiYeuCau;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YeuCauNhapHangRepository {

    public void save(YeuCauNhapHang ycnh) throws SQLException {
        String upsertSql = "INSERT INTO import_requests (request_code, created_by, created_date, status) " +
                           "VALUES (?, ?, ?, ?) " +
                           "ON CONFLICT (request_code) DO UPDATE SET created_by = EXCLUDED.created_by, " +
                           "created_date = EXCLUDED.created_date, status = EXCLUDED.status " +
                           "RETURNING id";
        try (Connection conn = KetNoiCSDL.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int requestId;
                try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                    pstmt.setString(1, ycnh.getRequestCode());
                    pstmt.setString(2, ycnh.getCreatedBy());
                    pstmt.setDate(3, java.sql.Date.valueOf(ycnh.getCreatedDate()));
                    pstmt.setString(4, ycnh.getStatus().name());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        rs.next();
                        requestId = rs.getInt("id");
                    }
                }

                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM merchandise_requests WHERE import_request_id = ?")) {
                    del.setInt(1, requestId);
                    del.executeUpdate();
                }

                if (!ycnh.getItems().isEmpty()) {
                    String itemSql = "INSERT INTO merchandise_requests " +
                            "(import_request_id, merchandise_code, merchandise_name, category, quantity_ordered, " +
                            "unit, stock_level, request_date, desired_delivery_date, supplier, estimated_price, notes) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                        for (ChiTietHangHoa item : ycnh.getItems()) {
                            pstmt.setInt(1, requestId);
                            pstmt.setString(2, item.getMerchandiseCode());
                            pstmt.setString(3, item.getMerchandiseName());
                            pstmt.setString(4, item.getCategory());
                            pstmt.setInt(5, item.getQuantityOrdered());
                            pstmt.setString(6, item.getUnit());
                            pstmt.setInt(7, item.getStockLevel());
                            pstmt.setDate(8, java.sql.Date.valueOf(item.getRequestDate()));
                            pstmt.setDate(9, java.sql.Date.valueOf(item.getDesiredDeliveryDate()));
                            pstmt.setString(10, item.getSupplier());
                            pstmt.setDouble(11, item.getEstimatedPrice());
                            pstmt.setString(12, item.getNotes());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public YeuCauNhapHang findByCode(String requestCode) throws SQLException {
        String sql = "SELECT ir.id AS req_id, ir.request_code, ir.created_by, ir.created_date, ir.status, " +
                     "mr.merchandise_code, mr.merchandise_name, mr.category, mr.quantity_ordered, mr.unit, " +
                     "mr.stock_level, mr.request_date, mr.desired_delivery_date, mr.supplier, mr.estimated_price, mr.notes " +
                     "FROM import_requests ir " +
                     "LEFT JOIN merchandise_requests mr ON mr.import_request_id = ir.id " +
                     "WHERE ir.request_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                YeuCauNhapHang req = null;
                while (rs.next()) {
                    if (req == null) {
                        req = new YeuCauNhapHang(
                            rs.getString("request_code"),
                            rs.getString("created_by"),
                            rs.getDate("created_date").toLocalDate(),
                            TrangThaiYeuCau.valueOf(rs.getString("status"))
                        );
                    }
                    String mercCode = rs.getString("merchandise_code");
                    if (mercCode != null) {
                        req.getItems().add(buildItem(rs));
                    }
                }
                return req;
            }
        }
    }

    public List<YeuCauNhapHang> findAll() throws SQLException {
        Map<Integer, YeuCauNhapHang> requestMap = new LinkedHashMap<>();
        String sql = "SELECT ir.id AS req_id, ir.request_code, ir.created_by, ir.created_date, ir.status, " +
                     "mr.merchandise_code, mr.merchandise_name, mr.category, mr.quantity_ordered, mr.unit, " +
                     "mr.stock_level, mr.request_date, mr.desired_delivery_date, mr.supplier, mr.estimated_price, mr.notes " +
                     "FROM import_requests ir " +
                     "LEFT JOIN merchandise_requests mr ON mr.import_request_id = ir.id " +
                     "ORDER BY ir.created_date DESC, ir.id";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int reqId = rs.getInt("req_id");
                if (!requestMap.containsKey(reqId)) {
                    requestMap.put(reqId, new YeuCauNhapHang(
                        rs.getString("request_code"),
                        rs.getString("created_by"),
                        rs.getDate("created_date").toLocalDate(),
                        TrangThaiYeuCau.valueOf(rs.getString("status"))
                    ));
                }
                String mercCode = rs.getString("merchandise_code");
                if (mercCode != null) {
                    requestMap.get(reqId).getItems().add(buildItem(rs));
                }
            }
        }
        return new ArrayList<>(requestMap.values());
    }

    public void delete(String requestCode) throws SQLException {
        String sql = "DELETE FROM import_requests WHERE request_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, requestCode);
            pstmt.executeUpdate();
        }
    }

    private ChiTietHangHoa buildItem(ResultSet rs) throws SQLException {
        return new ChiTietHangHoa(
            rs.getString("merchandise_code"),
            rs.getString("merchandise_name"),
            rs.getString("category"),
            rs.getInt("quantity_ordered"),
            rs.getString("unit"),
            rs.getInt("stock_level"),
            rs.getDate("request_date").toLocalDate(),
            rs.getDate("desired_delivery_date").toLocalDate(),
            rs.getString("supplier"),
            rs.getDouble("estimated_price"),
            rs.getString("notes")
        );
    }
}
