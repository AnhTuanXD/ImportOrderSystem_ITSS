package com.itss.importorder.database;

import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.SiteStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportSiteRepository {
    
    public void save(ImportSite site) throws SQLException {
        String sql = "INSERT INTO import_sites (site_code, name, password, delivery_days_by_ship, " +
                     "delivery_days_by_air, other_information, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (site_code) DO UPDATE SET name = EXCLUDED.name, " +
                     "password = EXCLUDED.password, delivery_days_by_ship = EXCLUDED.delivery_days_by_ship, " +
                     "delivery_days_by_air = EXCLUDED.delivery_days_by_air, " +
                     "other_information = EXCLUDED.other_information, status = EXCLUDED.status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, site.getSiteCode());
            pstmt.setString(2, site.getName());
            pstmt.setString(3, site.getPassword());
            pstmt.setInt(4, site.getDeliveryDaysByShip());
            pstmt.setInt(5, site.getDeliveryDaysByAir());
            pstmt.setString(6, site.getOtherInformation());
            pstmt.setString(7, site.getStatus().name());
            pstmt.executeUpdate();
        }
    }
    
    public ImportSite findByCode(String siteCode) throws SQLException {
        String sql = "SELECT * FROM import_sites WHERE site_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ImportSite(
                        rs.getString("site_code"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("delivery_days_by_ship"),
                        rs.getInt("delivery_days_by_air"),
                        rs.getString("other_information"),
                        SiteStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        }
        return null;
    }
    
    public List<ImportSite> findAll() throws SQLException {
        List<ImportSite> sites = new ArrayList<>();
        String sql = "SELECT * FROM import_sites ORDER BY site_code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sites.add(new ImportSite(
                    rs.getString("site_code"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getInt("delivery_days_by_ship"),
                    rs.getInt("delivery_days_by_air"),
                    rs.getString("other_information"),
                    SiteStatus.valueOf(rs.getString("status"))
                ));
            }
        }
        return sites;
    }
    
    public void delete(String siteCode) throws SQLException {
        String sql = "DELETE FROM import_sites WHERE site_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, siteCode);
            pstmt.executeUpdate();
        }
    }
}
