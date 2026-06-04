package com.itss.importorder.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KetNoiCSDL {
    private static final String URL = "jdbc:postgresql://localhost:5432/importorder_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi: PostgreSQL Driver không tìm thấy!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✓ Kết nối PostgreSQL thành công!");
            return true;
        } catch (SQLException e) {
            System.out.println("✗ Lỗi kết nối: " + e.getMessage());
            return false;
        }
    }
}
