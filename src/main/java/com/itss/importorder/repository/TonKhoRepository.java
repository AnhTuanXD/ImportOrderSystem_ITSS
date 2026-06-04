package com.itss.importorder.repository;

import com.itss.importorder.entity.TonKho;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TonKhoRepository {

    public void save(TonKho tonKho) throws SQLException {
        String sql = "INSERT INTO stock_records (site_code, merchandise_code, in_stock_quantity, unit) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (site_code, merchandise_code) DO UPDATE SET " +
                     "in_stock_quantity = EXCLUDED.in_stock_quantity, unit = EXCLUDED.unit";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tonKho.getSiteCode());
            pstmt.setString(2, tonKho.getMerchandiseCode());
            pstmt.setInt(3, tonKho.getInStockQuantity());
            pstmt.setString(4, tonKho.getUnit());
            pstmt.executeUpdate();
        }
    }

    public List<TonKho> findBySiteCode(String siteCode) throws SQLException {
        List<TonKho> list = new ArrayList<>();
        String sql = "SELECT * FROM stock_records WHERE site_code = ? ORDER BY merchandise_code";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TonKho(
                        rs.getString("site_code"),
                        rs.getString("merchandise_code"),
                        rs.getInt("in_stock_quantity"),
                        rs.getString("unit")
                    ));
                }
            }
        }
        return list;
    }

    public List<TonKho> findAll() throws SQLException {
        List<TonKho> list = new ArrayList<>();
        String sql = "SELECT * FROM stock_records ORDER BY site_code, merchandise_code";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TonKho(
                    rs.getString("site_code"),
                    rs.getString("merchandise_code"),
                    rs.getInt("in_stock_quantity"),
                    rs.getString("unit")
                ));
            }
        }
        return list;
    }

    public void delete(String siteCode, String merchandiseCode) throws SQLException {
        String sql = "DELETE FROM stock_records WHERE site_code = ? AND merchandise_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            pstmt.setString(2, merchandiseCode);
            pstmt.executeUpdate();
        }
    }
}
