package com.itss.importorder;

import com.itss.importorder.controller.PhuongAnController;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import com.itss.importorder.util.ValidationException;

public class TestRunner {
    public static void main(String[] args) {
        cleanDatabase();
        testPlanningSuccess();
        testPlanningFailsWhenStockIsMissing();
        testConfirmOrderUpdatesStock();
        System.out.println("All service tests passed.");
    }

    private static void cleanDatabase() {
        try (java.sql.Connection conn = com.itss.importorder.repository.KetNoiCSDL.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM import_plans WHERE request_code IN ('REQ-2026-001', 'REQ-2026-002')");
            stmt.executeUpdate("UPDATE import_requests SET status = 'SENT' WHERE request_code = 'REQ-2026-001'");
            stmt.executeUpdate("UPDATE import_requests SET status = 'PLANNING' WHERE request_code = 'REQ-2026-002'");
            stmt.executeUpdate("INSERT INTO stock_records (site_code, merchandise_code, merchandise_name, in_stock_quantity, unit) " +
                    "VALUES ('SJP01', 'CPU-I7', 'Intel Core i7', 80, 'pcs') ON CONFLICT (site_code, merchandise_code) DO UPDATE SET in_stock_quantity = 80");
            stmt.executeUpdate("INSERT INTO stock_records (site_code, merchandise_code, merchandise_name, in_stock_quantity, unit) " +
                    "VALUES ('SKR02', 'CPU-I7', 'Intel Core i7', 120, 'pcs') ON CONFLICT (site_code, merchandise_code) DO UPDATE SET in_stock_quantity = 120");
            stmt.executeUpdate("INSERT INTO stock_records (site_code, merchandise_code, merchandise_name, in_stock_quantity, unit) " +
                    "VALUES ('SSG03', 'CPU-I7', 'Intel Core i7', 40, 'pcs') ON CONFLICT (site_code, merchandise_code) DO UPDATE SET in_stock_quantity = 40");
        } catch (java.sql.SQLException e) {
            System.err.println("Warning: database cleanup failed: " + e.getMessage());
        }
    }

    private static void testPlanningSuccess() {
        DataStore store = SampleDataFactory.create();
        PhuongAnController controller = new PhuongAnController(store);
        YeuCauNhapHang ycnh;
        try {
            ycnh = store.findYeuCauNhapHangByCode("REQ-2026-001");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChiTietHangHoa item = ycnh.getItems().get(0);
        PhuongAnNhapHang phuongAn = controller.createAutomaticPlan(ycnh, item);
        assertTrue(phuongAn.getTotalQuantity() == item.getQuantityOrdered(), "Plan must satisfy requested quantity");
        assertTrue(!phuongAn.getAllocations().isEmpty(), "Plan must have allocations");
    }

    private static void testPlanningFailsWhenStockIsMissing() {
        DataStore store = SampleDataFactory.create();
        PhuongAnController controller = new PhuongAnController(store);
        YeuCauNhapHang ycnh;
        try {
            ycnh = store.findYeuCauNhapHangByCode("REQ-2026-002");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChiTietHangHoa item = ycnh.getItems().get(0);
        ChiTietHangHoa impossible = new ChiTietHangHoa("SSD-2T", "SSD 2TB NVMe", "Storage",
                9999, "pcs", 0, java.time.LocalDate.now(), item.getDesiredDeliveryDate(),
                "Test Supplier", 100.00, "Test note");
        boolean failed = false;
        try {
            controller.createAutomaticPlan(ycnh, impossible);
        } catch (ValidationException expected) {
            failed = true;
        }
        assertTrue(failed, "Planning must fail if stock is insufficient");
    }

    private static void testConfirmOrderUpdatesStock() {
        DataStore store = SampleDataFactory.create();
        com.itss.importorder.controller.YeuCauNhapHangController ycnhController = new com.itss.importorder.controller.YeuCauNhapHangController(store);
        PhuongAnController paController = new PhuongAnController(store);

        YeuCauNhapHang ycnh;
        try {
            ycnh = store.findYeuCauNhapHangByCode("REQ-2026-001");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ChiTietHangHoa item = ycnh.getItems().get(0);

        paController.createAutomaticPlan(ycnh, item);

        try {
            java.util.List<PhuongAnNhapHang> plans = store.findPhuongAnsByRequestCode(ycnh.getRequestCode());
            assertTrue(!plans.isEmpty(), "Plan should be saved in store");
            PhuongAnNhapHang plan = plans.get(0);
            assertTrue(plan.getAllocations().stream().anyMatch(a -> a.getSiteCode().equals("SJP01") && a.getQuantityOrdered() == 60), "SJP01 should be allocated 60 pcs");
            assertTrue(plan.getAllocations().stream().anyMatch(a -> a.getSiteCode().equals("SKR02") && a.getQuantityOrdered() == 120), "SKR02 should be allocated 120 pcs");

            int initialStockSJP = store.findTonKhosBySiteCode("SJP01").stream()
                    .filter(s -> s.getMerchandiseCode().equals("CPU-I7"))
                    .mapToInt(TonKho::getInStockQuantity)
                    .findFirst().orElse(-1);
            assertTrue(initialStockSJP == 80, "Initial stock at SJP01 should be 80");

            ycnhController.confirmOrderForSite(ycnh, "SJP01");

            assertTrue(ycnh.getStatus() == com.itss.importorder.entity.TrangThaiYeuCau.ORDERED, "Order status should be ORDERED");

            int updatedStockSJP = store.findTonKhosBySiteCode("SJP01").stream()
                    .filter(s -> s.getMerchandiseCode().equals("CPU-I7"))
                    .mapToInt(TonKho::getInStockQuantity)
                    .findFirst().orElse(-1);
            assertTrue(updatedStockSJP == 20, "Updated stock at SJP01 should be 20");

            int initialStockSKR = store.findTonKhosBySiteCode("SKR02").stream()
                    .filter(s -> s.getMerchandiseCode().equals("CPU-I7"))
                    .mapToInt(TonKho::getInStockQuantity)
                    .findFirst().orElse(-1);
            assertTrue(initialStockSKR == 120, "Initial stock at SKR02 should be 120");

            ycnhController.confirmOrderForSite(ycnh, "SKR02");

            int updatedStockSKR = store.findTonKhosBySiteCode("SKR02").stream()
                    .filter(s -> s.getMerchandiseCode().equals("CPU-I7"))
                    .mapToInt(TonKho::getInStockQuantity)
                    .findFirst().orElse(-1);
            assertTrue(updatedStockSKR == 0, "Updated stock at SKR02 should be 0");

        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
