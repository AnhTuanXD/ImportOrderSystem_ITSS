package com.itss.importorder.repository;

import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiaDiemNhapRepository {

    public void save(DiaDiemNhap diaDiem) throws SQLException {
        String sql = "INSERT INTO import_sites (site_code, name, tai_khoan, delivery_days_by_ship, " +
                     "delivery_days_by_air, other_information, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (site_code) DO UPDATE SET name = EXCLUDED.name, " +
                     "tai_khoan = EXCLUDED.tai_khoan, delivery_days_by_ship = EXCLUDED.delivery_days_by_ship, " +
                     "delivery_days_by_air = EXCLUDED.delivery_days_by_air, " +
                     "other_information = EXCLUDED.other_information, status = EXCLUDED.status";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, diaDiem.getSiteCode());
            pstmt.setString(2, diaDiem.getName());
            pstmt.setString(3, diaDiem.getTaiKhoan());
            pstmt.setInt(4, diaDiem.getDeliveryDaysByShip());
            pstmt.setInt(5, diaDiem.getDeliveryDaysByAir());
            pstmt.setString(6, diaDiem.getOtherInformation());
            pstmt.setString(7, diaDiem.getStatus().name());
            pstmt.executeUpdate();
        }
    }

    public DiaDiemNhap findByCode(String siteCode) throws SQLException {
        String sql = "SELECT * FROM import_sites WHERE site_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return buildDiaDiem(rs);
                }
            }
        }
        return null;
    }

    public List<DiaDiemNhap> findAll() throws SQLException {
        List<DiaDiemNhap> list = new ArrayList<>();
        String sql = "SELECT * FROM import_sites ORDER BY site_code";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(buildDiaDiem(rs));
            }
        }
        return list;
    }

    public void delete(String siteCode) throws SQLException {
        String sql = "DELETE FROM import_sites WHERE site_code = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            pstmt.executeUpdate();
        }
    }

    private DiaDiemNhap buildDiaDiem(ResultSet rs) throws SQLException {
        return new DiaDiemNhap(
            rs.getString("site_code"),
            rs.getString("name"),
            rs.getString("tai_khoan"),
            rs.getInt("delivery_days_by_ship"),
            rs.getInt("delivery_days_by_air"),
            rs.getString("other_information"),
            TrangThaiDiaDiem.valueOf(rs.getString("status"))
        );
    }
}
