package com.itss.importorder.controller;

import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.PhanBo;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.PhuongThucGiaoHang;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.util.ValidationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PhuongAnController {
    private final DataStore store;

    public PhuongAnController(DataStore store) {
        this.store = store;
    }

    public List<PhuongAnNhapHang> findAllPlans() {
        return store.getPhuongAnNhapHangs();
    }

    public List<PhuongAnNhapHang> findPlansByRequestCode(String requestCode) {
        try {
            return store.findPhuongAnsByRequestCode(requestCode);
        } catch (SQLException e) {
            System.err.println("Lỗi lấy phương án: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public PhuongAnNhapHang createAutomaticPlan(YeuCauNhapHang ycnh, ChiTietHangHoa item) {
        List<UngVien> ungViens = findUngViens(item);
        int remaining = item.getQuantityOrdered();
        PhuongAnNhapHang phuongAn = new PhuongAnNhapHang(
                nextPlanCode(), ycnh.getRequestCode(), item.getMerchandiseCode(), LocalDateTime.now());

        for (UngVien ungVien : ungViens) {
            if (remaining == 0) break;
            int ordered = Math.min(remaining, ungVien.tonKho.getInStockQuantity());
            phuongAn.getAllocations().add(new PhanBo(
                    ungVien.diaDiem.getSiteCode(), item.getMerchandiseCode(),
                    ordered, item.getUnit(), ungVien.phuongThucGiaoHang));
            remaining -= ordered;
        }

        if (remaining > 0) {
            throw new ValidationException("Không đủ tồn kho từ các Site đáp ứng ngày nhận mong muốn.");
        }

        try {
            store.savePhuongAnNhapHang(phuongAn);
            ycnh.setStatus(TrangThaiYeuCau.PLANNING);
            store.saveYeuCauNhapHang(ycnh);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu phương án: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return phuongAn;
    }

    public List<TonKho> findTonKhoFor(String merchandiseCode) {
        return store.getTonKhos().stream()
                .filter(tk -> tk.getMerchandiseCode().equalsIgnoreCase(merchandiseCode))
                .collect(Collectors.toList());
    }

    public List<UngVienPreview> previewUngViens(ChiTietHangHoa item) {
        return findUngViens(item).stream()
                .map(uv -> new UngVienPreview(
                        uv.diaDiem.getSiteCode(), uv.diaDiem.getName(),
                        uv.tonKho.getInStockQuantity(), uv.tonKho.getUnit(),
                        uv.phuongThucGiaoHang, uv.soNgayGiao))
                .collect(Collectors.toList());
    }

    private List<UngVien> findUngViens(ChiTietHangHoa item) {
        LocalDate today = LocalDate.now();
        List<UngVien> ungViens = new ArrayList<>();

        for (TonKho tonKho : store.getTonKhos()) {
            if (!tonKho.getMerchandiseCode().equalsIgnoreCase(item.getMerchandiseCode())
                    || tonKho.getInStockQuantity() <= 0) {
                continue;
            }
            Optional<DiaDiemNhap> diaDiemOpt = store.getDiaDiemNhaps().stream()
                    .filter(d -> d.getSiteCode().equals(tonKho.getSiteCode()))
                    .filter(d -> d.getStatus() == TrangThaiDiaDiem.ACTIVE)
                    .findFirst();
            if (diaDiemOpt.isEmpty()) continue;

            DiaDiemNhap diaDiem = diaDiemOpt.get();
            boolean shipCanMeet = !today.plusDays(diaDiem.getDeliveryDaysByShip())
                    .isAfter(item.getDesiredDeliveryDate());
            boolean airCanMeet = !today.plusDays(diaDiem.getDeliveryDaysByAir())
                    .isAfter(item.getDesiredDeliveryDate());

            if (shipCanMeet) {
                ungViens.add(new UngVien(diaDiem, tonKho, PhuongThucGiaoHang.SHIP, diaDiem.getDeliveryDaysByShip()));
            } else if (airCanMeet) {
                ungViens.add(new UngVien(diaDiem, tonKho, PhuongThucGiaoHang.AIR, diaDiem.getDeliveryDaysByAir()));
            }
        }

        ungViens.sort(Comparator
                .comparing((UngVien uv) -> uv.phuongThucGiaoHang == PhuongThucGiaoHang.SHIP ? 0 : 1)
                .thenComparing((UngVien uv) -> uv.tonKho.getInStockQuantity(), Comparator.reverseOrder())
                .thenComparing(uv -> uv.soNgayGiao)
                .thenComparing(uv -> uv.diaDiem.getSiteCode()));
        return ungViens;
    }

    private String nextPlanCode() {
        return "PLAN-" + String.format("%03d", store.getPhuongAnNhapHangs().size() + 1);
    }

    private static class UngVien {
        private final DiaDiemNhap diaDiem;
        private final TonKho tonKho;
        private final PhuongThucGiaoHang phuongThucGiaoHang;
        private final int soNgayGiao;

        private UngVien(DiaDiemNhap diaDiem, TonKho tonKho, PhuongThucGiaoHang phuongThucGiaoHang, int soNgayGiao) {
            this.diaDiem = diaDiem;
            this.tonKho = tonKho;
            this.phuongThucGiaoHang = phuongThucGiaoHang;
            this.soNgayGiao = soNgayGiao;
        }
    }

    public static class UngVienPreview {
        private final String siteCode;
        private final String siteName;
        private final int inStockQuantity;
        private final String unit;
        private final PhuongThucGiaoHang phuongThucGiaoHang;
        private final int soNgayGiao;

        public UngVienPreview(String siteCode, String siteName, int inStockQuantity, String unit,
                              PhuongThucGiaoHang phuongThucGiaoHang, int soNgayGiao) {
            this.siteCode = siteCode;
            this.siteName = siteName;
            this.inStockQuantity = inStockQuantity;
            this.unit = unit;
            this.phuongThucGiaoHang = phuongThucGiaoHang;
            this.soNgayGiao = soNgayGiao;
        }

        public String getSiteCode() { return siteCode; }
        public String getSiteName() { return siteName; }
        public int getInStockQuantity() { return inStockQuantity; }
        public String getUnit() { return unit; }
        public PhuongThucGiaoHang getPhuongThucGiaoHang() { return phuongThucGiaoHang; }
        public int getSoNgayGiao() { return soNgayGiao; }
    }
}
