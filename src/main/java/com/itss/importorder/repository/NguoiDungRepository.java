package com.itss.importorder.repository;

import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.VaiTro;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungRepository {

    public void save(NguoiDung nguoiDung) throws SQLException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?) " +
                     "ON CONFLICT (username) DO UPDATE SET password = EXCLUDED.password, role = EXCLUDED.role";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nguoiDung.getUsername());
            pstmt.setString(2, nguoiDung.getPassword());
            pstmt.setString(3, nguoiDung.getVaiTro().name());
            pstmt.executeUpdate();
        }
    }

    public NguoiDung findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new NguoiDung(
                        rs.getString("username"),
                        rs.getString("password"),
                        VaiTro.valueOf(rs.getString("role"))
                    );
                }
            }
        }
        return null;
    }

    public List<NguoiDung> findAll() throws SQLException {
        List<NguoiDung> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        try (Connection conn = KetNoiCSDL.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new NguoiDung(
                    rs.getString("username"),
                    rs.getString("password"),
                    VaiTro.valueOf(rs.getString("role"))
                ));
            }
        }
        return list;
    }

    public void delete(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = KetNoiCSDL.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }
}
