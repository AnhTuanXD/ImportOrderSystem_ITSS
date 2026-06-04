package com.itss.importorder.repository;

import com.itss.importorder.database.*;
import com.itss.importorder.model.ImportPlan;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.StockRecord;
import com.itss.importorder.model.User;
import com.itss.importorder.model.WarehouseReport;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private final UserRepository userRepository = new UserRepository();
    private final ImportRequestRepository importRequestRepository = new ImportRequestRepository();
    private final ImportSiteRepository importSiteRepository = new ImportSiteRepository();
    private final StockRecordRepository stockRecordRepository = new StockRecordRepository();
    private final ImportPlanRepository importPlanRepository = new ImportPlanRepository();
    private final WarehouseReportRepository warehouseReportRepository = new WarehouseReportRepository();

    public List<User> getUsers() {
        try {
            return userRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ImportRequest> getImportRequests() {
        try {
            return importRequestRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu import requests: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ImportSite> getImportSites() {
        try {
            return importSiteRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu import sites: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<StockRecord> getStockRecords() {
        try {
            return stockRecordRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu stock records: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ImportPlan> getImportPlans() {
        try {
            return importPlanRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu import plans: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<WarehouseReport> getWarehouseReports() {
        try {
            return warehouseReportRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu warehouse reports: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Các phương thức save
    public void saveUser(User user) throws SQLException {
        userRepository.save(user);
    }

    public void saveImportRequest(ImportRequest request) throws SQLException {
        importRequestRepository.save(request);
    }

    public void saveImportSite(ImportSite site) throws SQLException {
        importSiteRepository.save(site);
    }

    public void saveStockRecord(StockRecord record) throws SQLException {
        stockRecordRepository.save(record);
    }

    public void saveImportPlan(ImportPlan plan) throws SQLException {
        importPlanRepository.save(plan);
    }

    public void saveWarehouseReport(WarehouseReport report) throws SQLException {
        warehouseReportRepository.save(report);
    }

    // Các phương thức delete
    public void deleteUser(String username) throws SQLException {
        userRepository.delete(username);
    }

    public void deleteImportRequest(String requestCode) throws SQLException {
        importRequestRepository.delete(requestCode);
    }

    public void deleteImportSite(String siteCode) throws SQLException {
        importSiteRepository.delete(siteCode);
    }

    public void deleteStockRecord(String siteCode, String merchandiseCode) throws SQLException {
        stockRecordRepository.delete(siteCode, merchandiseCode);
    }

    public void deleteImportPlan(String planCode) throws SQLException {
        importPlanRepository.delete(planCode);
    }

    public void deleteWarehouseReport(String reportCode) throws SQLException {
        warehouseReportRepository.delete(reportCode);
    }

    // Các phương thức find
    public User findUserByUsername(String username) throws SQLException {
        return userRepository.findByUsername(username);
    }

    public ImportRequest findImportRequestByCode(String requestCode) throws SQLException {
        return importRequestRepository.findByCode(requestCode);
    }

    public ImportSite findImportSiteByCode(String siteCode) throws SQLException {
        return importSiteRepository.findByCode(siteCode);
    }

    public List<StockRecord> findStockRecordsBySiteCode(String siteCode) throws SQLException {
        return stockRecordRepository.findBySiteCode(siteCode);
    }

    public ImportPlan findImportPlanByCode(String planCode) throws SQLException {
        return importPlanRepository.findByCode(planCode);
    }

    public List<ImportPlan> findImportPlansByRequestCode(String requestCode) throws SQLException {
        return importPlanRepository.findByRequestCode(requestCode);
    }

    public WarehouseReport findWarehouseReportByCode(String reportCode) throws SQLException {
        return warehouseReportRepository.findByCode(reportCode);
    }

    public List<WarehouseReport> findWarehouseReportsByRequestCode(String requestCode) throws SQLException {
        return warehouseReportRepository.findByRequestCode(requestCode);
    }
}

