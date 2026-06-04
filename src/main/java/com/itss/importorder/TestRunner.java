package com.itss.importorder;

import com.itss.importorder.controller.PhuongAnController;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import com.itss.importorder.util.ValidationException;

public class TestRunner {
    public static void main(String[] args) {
        testPlanningSuccess();
        testPlanningFailsWhenStockIsMissing();
        System.out.println("All service tests passed.");
    }

    private static void testPlanningSuccess() {
        DataStore store = SampleDataFactory.create();
        PhuongAnController controller = new PhuongAnController(store);
        YeuCauNhapHang ycnh = store.getYeuCauNhapHangs().get(0);
        ChiTietHangHoa item = ycnh.getItems().get(0);
        PhuongAnNhapHang phuongAn = controller.createAutomaticPlan(ycnh, item);
        assertTrue(phuongAn.getTotalQuantity() == item.getQuantityOrdered(), "Plan must satisfy requested quantity");
        assertTrue(!phuongAn.getAllocations().isEmpty(), "Plan must have allocations");
    }

    private static void testPlanningFailsWhenStockIsMissing() {
        DataStore store = SampleDataFactory.create();
        PhuongAnController controller = new PhuongAnController(store);
        YeuCauNhapHang ycnh = store.getYeuCauNhapHangs().get(1);
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

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
