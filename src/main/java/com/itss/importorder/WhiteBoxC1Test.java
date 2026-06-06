package com.itss.importorder;

import com.itss.importorder.controller.YeuCauNhapHangController;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import com.itss.importorder.util.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WhiteBoxC1Test {

    public static void runC1Tests() {
        DataStore store = SampleDataFactory.create();
        YeuCauNhapHangController controller = new YeuCauNhapHangController(store);

        System.out.println("Bắt đầu chạy kiểm thử hộp trắng C1...");

        // =================================================================
        // Test Case 1: Phủ nhánh True của Quyết định 1 (Danh sách rỗng)
        // =================================================================
        try {
            controller.create("sales", null);
            fail("TC1 Thất bại: Không ném ngoại lệ khi items null");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Yêu cầu phải có ít nhất một mặt hàng"), "TC1: Pass");
        }

        // =================================================================
        // Test Case 2: Phủ nhánh True của Quyết định 3 (Mã hàng trống)
        // =================================================================
        List<ChiTietHangHoa> list2 = new ArrayList<>();
        list2.add(createItem("", "Tên hàng", 10, "PCS", LocalDate.now(), LocalDate.now().plusDays(5), 0.0));
        try {
            controller.create("sales", list2);
            fail("TC2 Thất bại: Không ném ngoại lệ khi mã hàng trống");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Mã hàng không được để trống"), "TC2: Pass");
        }

        // =================================================================
        // Test Case 3: Phủ nhánh True của Quyết định 4 (Tên hàng trống)
        // =================================================================
        List<ChiTietHangHoa> list3 = new ArrayList<>();
        list3.add(createItem("CPU-I7", "", 10, "PCS", LocalDate.now(), LocalDate.now().plusDays(5), 0.0));
        try {
            controller.create("sales", list3);
            fail("TC3 Thất bại: Không ném ngoại lệ khi tên hàng trống");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Tên mặt hàng không được để trống"), "TC3: Pass");
        }

        // =================================================================
        // Test Case 4: Phủ nhánh True của Quyết định 5 (Số lượng <= 0)
        // =================================================================
        List<ChiTietHangHoa> list4 = new ArrayList<>();
        list4.add(createItem("CPU-I7", "Tên hàng", 0, "PCS", LocalDate.now(), LocalDate.now().plusDays(5), 0.0));
        try {
            controller.create("sales", list4);
            fail("TC4 Thất bại: Không ném ngoại lệ khi số lượng = 0");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Số lượng đặt phải lớn hơn 0"), "TC4: Pass");
        }

        // =================================================================
        // Test Case 5: Phủ nhánh True của Quyết định 6 (Đơn vị trống)
        // =================================================================
        List<ChiTietHangHoa> list5 = new ArrayList<>();
        list5.add(createItem("CPU-I7", "Tên hàng", 10, "", LocalDate.now(), LocalDate.now().plusDays(5), 0.0));
        try {
            controller.create("sales", list5);
            fail("TC5 Thất bại: Không ném ngoại lệ khi đơn vị trống");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Đơn vị không được để trống"), "TC5: Pass");
        }

        // =================================================================
        // Test Case 6: Phủ nhánh True của Quyết định 7 (Ngày yêu cầu null)
        // =================================================================
        List<ChiTietHangHoa> list6 = new ArrayList<>();
        list6.add(createItem("CPU-I7", "Tên hàng", 10, "PCS", null, LocalDate.now().plusDays(5), 0.0));
        try {
            controller.create("sales", list6);
            fail("TC6 Thất bại: Không ném ngoại lệ khi ngày yêu cầu null");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Ngày yêu cầu không được để trống"), "TC6: Pass");
        }

        // =================================================================
        // Test Case 7: Phủ nhánh True của Quyết định 8 (Ngày cần hàng không hợp lệ)
        // =================================================================
        List<ChiTietHangHoa> list7 = new ArrayList<>();
        list7.add(createItem("CPU-I7", "Tên hàng", 10, "PCS", LocalDate.now(), LocalDate.now().minusDays(2), 0.0));
        try {
            controller.create("sales", list7);
            fail("TC7 Thất bại: Không ném ngoại lệ khi ngày cần hàng trước ngày yêu cầu");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Ngày cần hàng phải sau ngày yêu cầu"), "TC7: Pass");
        }

        // =================================================================
        // Test Case 8: Phủ nhánh True của Quyết định 9 (Giá ước tính âm)
        // =================================================================
        List<ChiTietHangHoa> list8 = new ArrayList<>();
        list8.add(createItem("CPU-I7", "Tên hàng", 10, "PCS", LocalDate.now(), LocalDate.now().plusDays(5), -20.0));
        try {
            controller.create("sales", list8);
            fail("TC8 Thất bại: Không ném ngoại lệ khi giá âm");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Giá ước tính không được âm"), "TC8: Pass");
        }

        // =================================================================
        // Test Case 9: Happy Path (Phủ toàn bộ nhánh False và Nhánh 2.B thoát vòng lặp)
        // =================================================================
        List<ChiTietHangHoa> list9 = new ArrayList<>();
        list9.add(createItem("CPU-I7", "Intel Core i7", 10, "PCS", LocalDate.now(), LocalDate.now().plusDays(10), 450.0));
        try {
            controller.create("sales", list9);
            System.out.println("TC9: Pass (Tạo thành công yêu cầu nhập hàng hợp lệ)");
        } catch (Exception e) {
            fail("TC9 Thất bại: Gặp lỗi không mong đợi: " + e.getMessage());
        }

        System.out.println("Hoàn tất 100% C1 coverage testing cho validateItems().");
    }

    // Helper tạo nhanh thực thể ChiTietHangHoa
    private static ChiTietHangHoa createItem(String code, String name, int qty, String unit, 
                                             LocalDate reqDate, LocalDate desDate, double price) {
        return new ChiTietHangHoa(code, name, "Electronics", qty, unit, 0, reqDate, desDate, "Supplier", price, "Note");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("AssertTrue Fail: " + message);
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}
