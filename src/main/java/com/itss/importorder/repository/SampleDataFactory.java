package com.itss.importorder.repository;

import com.itss.importorder.model.ImportPlan;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.model.RequestStatus;
import com.itss.importorder.model.Role;
import com.itss.importorder.model.SiteStatus;
import com.itss.importorder.model.StockRecord;
import com.itss.importorder.model.User;
import java.sql.SQLException;
import java.time.LocalDate;

public final class SampleDataFactory {
    private SampleDataFactory() {
    }

    public static DataStore create() {
        DataStore store = new DataStore();
        runSeed("users",    () -> seedUsers(store));
        runSeed("sites",    () -> seedSites(store));
        runSeed("stocks",   () -> seedStocks(store));
        runSeed("requests", () -> seedRequests(store));
        return store;
    }

    @FunctionalInterface
    private interface SeedTask {
        void run() throws SQLException;
    }

    private static void runSeed(String name, SeedTask task) {
        try {
            task.run();
        } catch (SQLException e) {
            System.err.println("✗ Lỗi khi lưu dữ liệu mẫu [" + name + "]: " + e.getMessage());
        }
    }

    private static void seedUsers(DataStore store) throws SQLException {
        store.saveUser(new User("sales", "123456", Role.SALES));
        store.saveUser(new User("overseas", "123456", Role.OVERSEAS_ORDER));
        store.saveUser(new User("site", "123456", Role.IMPORT_SITE));
        store.saveUser(new User("warehouse", "123456", Role.WAREHOUSE));
    }

    private static void seedSites(DataStore store) throws SQLException {
        store.saveImportSite(new ImportSite("SJP01", "Tokyo Trade Hub", "123456", 12, 4,
                "Chuyên linh kiện điện tử", SiteStatus.ACTIVE));
        store.saveImportSite(new ImportSite("SKR02", "Seoul Parts Center", "123456", 10, 3,
                "Nguồn hàng ổn định", SiteStatus.ACTIVE));
        store.saveImportSite(new ImportSite("SSG03", "Singapore Global Import", "123456", 8, 2,
                "Ưu tiên giao nhanh", SiteStatus.ACTIVE));
        store.saveImportSite(new ImportSite("SDE04", "Berlin Machinery", "123456", 25, 7,
                "Máy móc công nghiệp", SiteStatus.DISABLED));
    }

    private static void seedStocks(DataStore store) throws SQLException {
        store.saveStockRecord(new StockRecord("SJP01", "CPU-I7", 80, "pcs"));
        store.saveStockRecord(new StockRecord("SKR02", "CPU-I7", 120, "pcs"));
        store.saveStockRecord(new StockRecord("SSG03", "CPU-I7", 40, "pcs"));
        store.saveStockRecord(new StockRecord("SJP01", "RAM-32", 150, "pcs"));
        store.saveStockRecord(new StockRecord("SKR02", "RAM-32", 60, "pcs"));
        store.saveStockRecord(new StockRecord("SSG03", "SSD-2T", 95, "pcs"));
        store.saveStockRecord(new StockRecord("SJP01", "SSD-2T", 30, "pcs"));
        store.saveStockRecord(new StockRecord("SDE04", "MOTOR-A2", 300, "pcs"));
    }

    private static void seedRequests(DataStore store) throws SQLException {
        ImportRequest req1 = new ImportRequest("REQ-2025-001", "sales", LocalDate.now().minusDays(2),
                RequestStatus.SENT);
        req1.getItems().add(new MerchandiseRequest("CPU-I7", "Intel Core i7", "Processor", 180, "pcs", 500, 
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(14), "Tokyo Tech Supplier", 450.50, "Ghi chú CPU"));
        store.saveImportRequest(req1);

        ImportRequest req2 = new ImportRequest("REQ-2025-002", "sales", LocalDate.now().minusDays(1),
                RequestStatus.PLANNING);
        req2.getItems().add(new MerchandiseRequest("SSD-2T", "SSD 2TB NVMe", "Storage", 100, "pcs", 200,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(9), "Singapore Storage Ltd", 280.00, "Ghi chú SSD"));
        store.saveImportRequest(req2);

        ImportRequest req3 = new ImportRequest("REQ-2025-003", "sales", LocalDate.now(),
                RequestStatus.RECEIVED);
        req3.getItems().add(new MerchandiseRequest("MOUSE-PRO", "Wireless Gaming Mouse", "Peripheral", 50, "pcs", 100,
                LocalDate.now(), LocalDate.now().plusDays(4), "Tech Accessories Asia", 35.50, "Ghi chú Chuột"));
        store.saveImportRequest(req3);
        
        store.saveImportPlan(new ImportPlan("PLAN-000", "REQ-2025-003", "MOUSE-PRO",
                java.time.LocalDateTime.now().minusDays(1)));
    }
}

