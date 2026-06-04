package com.itss.importorder.repository;

import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.entity.YeuCauNhapHang;
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
        store.saveNguoiDung(new NguoiDung("sales",     "123456", VaiTro.SALES));
        store.saveNguoiDung(new NguoiDung("overseas",  "123456", VaiTro.OVERSEAS_ORDER));
        store.saveNguoiDung(new NguoiDung("site",      "123456", VaiTro.IMPORT_SITE));
        store.saveNguoiDung(new NguoiDung("warehouse", "123456", VaiTro.WAREHOUSE));
    }

    private static void seedSites(DataStore store) throws SQLException {
        store.saveDiaDiemNhap(new DiaDiemNhap("SJP01", "Tokyo Trade Hub",         "123456", 12, 4,
                "Chuyên linh kiện điện tử", TrangThaiDiaDiem.ACTIVE));
        store.saveDiaDiemNhap(new DiaDiemNhap("SKR02", "Seoul Parts Center",      "123456", 10, 3,
                "Nguồn hàng ổn định", TrangThaiDiaDiem.ACTIVE));
        store.saveDiaDiemNhap(new DiaDiemNhap("SSG03", "Singapore Global Import", "123456",  8, 2,
                "Ưu tiên giao nhanh", TrangThaiDiaDiem.ACTIVE));
        store.saveDiaDiemNhap(new DiaDiemNhap("SDE04", "Berlin Machinery",        "123456", 25, 7,
                "Máy móc công nghiệp", TrangThaiDiaDiem.DISABLED));
    }

    private static void seedStocks(DataStore store) throws SQLException {
        store.saveTonKho(new TonKho("SJP01", "CPU-I7",    80,  "pcs"));
        store.saveTonKho(new TonKho("SKR02", "CPU-I7",   120,  "pcs"));
        store.saveTonKho(new TonKho("SSG03", "CPU-I7",    40,  "pcs"));
        store.saveTonKho(new TonKho("SJP01", "RAM-32",   150,  "pcs"));
        store.saveTonKho(new TonKho("SKR02", "RAM-32",    60,  "pcs"));
        store.saveTonKho(new TonKho("SSG03", "SSD-2T",    95,  "pcs"));
        store.saveTonKho(new TonKho("SJP01", "SSD-2T",    30,  "pcs"));
        store.saveTonKho(new TonKho("SDE04", "MOTOR-A2", 300,  "pcs"));
        store.saveTonKho(new TonKho("site",  "CPU-I7",    80,  "pcs"));
        store.saveTonKho(new TonKho("site",  "RAM-32",   150,  "pcs"));
        store.saveTonKho(new TonKho("site",  "SSD-2T",    30,  "pcs"));
        store.saveTonKho(new TonKho("site",  "MONITOR-4K", 45, "pcs"));
    }

    private static void seedRequests(DataStore store) throws SQLException {
        YeuCauNhapHang req1 = new YeuCauNhapHang("REQ-2025-001", "sales",
                LocalDate.now().minusDays(2), TrangThaiYeuCau.SENT);
        req1.getItems().add(new ChiTietHangHoa("CPU-I7", "Intel Core i7", "Processor", 180, "pcs", 500,
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(14),
                "Tokyo Tech Supplier", 450.50, "Ghi chú CPU"));
        store.saveYeuCauNhapHang(req1);

        YeuCauNhapHang req2 = new YeuCauNhapHang("REQ-2025-002", "sales",
                LocalDate.now().minusDays(1), TrangThaiYeuCau.PLANNING);
        req2.getItems().add(new ChiTietHangHoa("SSD-2T", "SSD 2TB NVMe", "Storage", 100, "pcs", 200,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(9),
                "Singapore Storage Ltd", 280.00, "Ghi chú SSD"));
        store.saveYeuCauNhapHang(req2);

        YeuCauNhapHang req3 = new YeuCauNhapHang("REQ-2025-003", "sales",
                LocalDate.now(), TrangThaiYeuCau.RECEIVED);
        req3.getItems().add(new ChiTietHangHoa("MOUSE-PRO", "Wireless Gaming Mouse", "Peripheral", 50, "pcs", 100,
                LocalDate.now(), LocalDate.now().plusDays(4),
                "Tech Accessories Asia", 35.50, "Ghi chú Chuột"));
        store.saveYeuCauNhapHang(req3);

        store.savePhuongAnNhapHang(new PhuongAnNhapHang("PLAN-000", "REQ-2025-003", "MOUSE-PRO",
                java.time.LocalDateTime.now().minusDays(1)));
    }
}
