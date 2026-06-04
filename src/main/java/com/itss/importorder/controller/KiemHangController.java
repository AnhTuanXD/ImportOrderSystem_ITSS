package com.itss.importorder.controller;

import com.itss.importorder.entity.BaoCaoKho;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.util.ValidationException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KiemHangController {
    private final DataStore store;

    public KiemHangController(DataStore store) {
        this.store = store;
    }

    public List<YeuCauNhapHang> findOrdersForChecking() {
        return store.getYeuCauNhapHangs().stream()
                .filter(r -> r.getStatus() == TrangThaiYeuCau.ORDERED)
                .sorted(Comparator.comparing(YeuCauNhapHang::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }

    public BaoCaoKho createReport(YeuCauNhapHang ycnh, String checker, String result, String note) {
        if (checker == null || checker.isBlank()) {
            throw new ValidationException("Người kiểm tra không được để trống.");
        }
        if (result == null || result.isBlank()) {
            throw new ValidationException("Kết quả kiểm tra không được để trống.");
        }
        BaoCaoKho baoCao = new BaoCaoKho(nextReportCode(), ycnh.getRequestCode(), checker,
                LocalDateTime.now(), result, note == null ? "" : note);
        try {
            store.saveBaoCaoKho(baoCao);
            ycnh.setStatus(TrangThaiYeuCau.RECEIVED);
            store.saveYeuCauNhapHang(ycnh);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu báo cáo kho: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return baoCao;
    }

    public List<BaoCaoKho> findReports() {
        return store.getBaoCaoKhos();
    }

    private String nextReportCode() {
        return "WHR-" + String.format("%03d", store.getBaoCaoKhos().size() + 1);
    }
}
