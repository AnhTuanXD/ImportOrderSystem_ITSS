package com.itss.importorder.controller;

import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.PhanBo;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.util.ValidationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class YeuCauNhapHangController {
    private final DataStore store;

    public YeuCauNhapHangController(DataStore store) {
        this.store = store;
    }

    /** createdBy = null → trả về tất cả; có giá trị → chỉ trả về của tài khoản đó */
    public List<YeuCauNhapHang> findAll(String createdBy) {
        return store.getYeuCauNhapHangs().stream()
                .filter(r -> createdBy == null || r.getCreatedBy().equals(createdBy))
                .sorted(Comparator.comparing(YeuCauNhapHang::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }

    public List<YeuCauNhapHang> search(String keyword, String createdBy) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(createdBy);
        }
        String normalized = keyword.toLowerCase(Locale.ROOT).trim();
        return findAll(createdBy).stream()
                .filter(r -> r.getRequestCode().toLowerCase(Locale.ROOT).contains(normalized)
                        || r.getStatus().getDisplayName().toLowerCase(Locale.ROOT).contains(normalized)
                        || r.getItems().stream().anyMatch(item ->
                                item.getMerchandiseCode().toLowerCase(Locale.ROOT).contains(normalized)))
                .collect(Collectors.toList());
    }

    public YeuCauNhapHang create(String createdBy, List<ChiTietHangHoa> items) {
        validateItems(items);
        int maxNum = 0;
        for (YeuCauNhapHang r : store.getYeuCauNhapHangs()) {
            String requestCode = r.getRequestCode();
            if (requestCode != null) {
                int lastDash = requestCode.lastIndexOf('-');
                if (lastDash >= 0 && lastDash < requestCode.length() - 1) {
                    try {
                        int num = Integer.parseInt(requestCode.substring(lastDash + 1));
                        if (num > maxNum) {
                            maxNum = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        String code = "REQ-2026-" + String.format("%03d", maxNum + 1);
        YeuCauNhapHang ycnh = new YeuCauNhapHang(code, createdBy, LocalDate.now(), TrangThaiYeuCau.SENT);
        ycnh.getItems().addAll(items);
        try {
            store.saveYeuCauNhapHang(ycnh);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu yêu cầu vào database: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return ycnh;
    }

    public void updateItems(YeuCauNhapHang ycnh, List<ChiTietHangHoa> items) {
        validateItems(items);
        ycnh.getItems().clear();
        ycnh.getItems().addAll(items);
        try {
            store.saveYeuCauNhapHang(ycnh);
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật yêu cầu: " + e.getMessage());
            throw new ValidationException("Lỗi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    public void delete(YeuCauNhapHang ycnh) {
        try {
            store.deleteYeuCauNhapHang(ycnh.getRequestCode());
        } catch (SQLException e) {
            System.err.println("Lỗi xóa yêu cầu: " + e.getMessage());
            throw new ValidationException("Lỗi xóa dữ liệu: " + e.getMessage());
        }
    }

    public void updateTrangThai(YeuCauNhapHang ycnh, TrangThaiYeuCau trangThai) {
        ycnh.setStatus(trangThai);
        try {
            store.saveYeuCauNhapHang(ycnh);
        } catch (SQLException e) {
            throw new ValidationException("Lỗi cập nhật trạng thái: " + e.getMessage());
        }
    }

    public void confirmOrderForSite(YeuCauNhapHang ycnh, String siteCode) {
        try {
            List<PhuongAnNhapHang> plans = store.findPhuongAnsByRequestCode(ycnh.getRequestCode());
            boolean updatedAny = false;
            for (PhuongAnNhapHang plan : plans) {
                for (PhanBo alloc : plan.getAllocations()) {
                    if (alloc.getSiteCode().equalsIgnoreCase(siteCode) && !alloc.isConfirmed()) {
                        // 1. Mark as confirmed in DB
                        store.confirmAllocation(plan.getPlanCode(), siteCode, alloc.getMerchandiseCode());
                        alloc.setConfirmed(true);
                        updatedAny = true;

                        // 2. Deduct stock
                        List<TonKho> stocks = store.findTonKhosBySiteCode(siteCode);
                        Optional<TonKho> stockOpt = stocks.stream()
                                .filter(s -> s.getMerchandiseCode().equalsIgnoreCase(alloc.getMerchandiseCode()))
                                .findFirst();

                        if (stockOpt.isPresent()) {
                            TonKho stock = stockOpt.get();
                            int newQty = stock.getInStockQuantity() - alloc.getQuantityOrdered();
                            if (newQty < 0) {
                                newQty = 0;
                            }
                            stock.setInStockQuantity(newQty);
                            store.saveTonKho(stock);
                        } else {
                            String merchName = ycnh.getItems().stream()
                                    .filter(item -> item.getMerchandiseCode().equalsIgnoreCase(alloc.getMerchandiseCode()))
                                    .map(ChiTietHangHoa::getMerchandiseName)
                                    .findFirst()
                                    .orElse(alloc.getMerchandiseCode());
                            TonKho stock = new TonKho(siteCode, alloc.getMerchandiseCode(), merchName, 0, alloc.getUnit());
                            store.saveTonKho(stock);
                        }
                    }
                }
            }

            if (updatedAny) {
                // Fetch the updated plans to see if all allocations of all plans are now confirmed
                List<PhuongAnNhapHang> updatedPlans = store.findPhuongAnsByRequestCode(ycnh.getRequestCode());
                boolean allConfirmed = true;
                for (PhuongAnNhapHang plan : updatedPlans) {
                    for (PhanBo alloc : plan.getAllocations()) {
                        if (!alloc.isConfirmed()) {
                            allConfirmed = false;
                            break;
                        }
                    }
                    if (!allConfirmed) break;
                }

                if (allConfirmed) {
                    ycnh.setStatus(TrangThaiYeuCau.ORDERED);
                    store.saveYeuCauNhapHang(ycnh);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException("Lỗi cập nhật trạng thái đơn hàng và tồn kho: " + e.getMessage());
        }
    }

    /** Trả về các đơn hàng PLANNING được phân bổ cho site này */
    public List<YeuCauNhapHang> findPlanningForSite(String siteCode) {
        java.util.Set<String> requestCodes = store.getPhuongAnNhapHangs().stream()
                .filter(p -> p.getAllocations().stream()
                        .anyMatch(a -> a.getSiteCode().equals(siteCode) && !a.isConfirmed()))
                .map(com.itss.importorder.entity.PhuongAnNhapHang::getRequestCode)
                .collect(java.util.stream.Collectors.toSet());
        return store.getYeuCauNhapHangs().stream()
                .filter(r -> r.getStatus() == TrangThaiYeuCau.PLANNING
                        && requestCodes.contains(r.getRequestCode()))
                .sorted(Comparator.comparing(YeuCauNhapHang::getCreatedDate).reversed())
                .collect(Collectors.toList());
    }

    public Optional<YeuCauNhapHang> findByCode(String requestCode) {
        return store.getYeuCauNhapHangs().stream()
                .filter(r -> r.getRequestCode().equals(requestCode))
                .findFirst();
    }

    private void validateItems(List<ChiTietHangHoa> items) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Yêu cầu phải có ít nhất một mặt hàng.");
        }
        for (ChiTietHangHoa item : items) {
            if (item.getMerchandiseCode() == null || item.getMerchandiseCode().isBlank()) {
                throw new ValidationException("Mã hàng không được để trống.");
            }
            if (item.getMerchandiseName() == null || item.getMerchandiseName().isBlank()) {
                throw new ValidationException("Tên mặt hàng không được để trống.");
            }
            if (item.getQuantityOrdered() <= 0) {
                throw new ValidationException("Số lượng đặt phải lớn hơn 0.");
            }
            if (item.getUnit() == null || item.getUnit().isBlank()) {
                throw new ValidationException("Đơn vị không được để trống.");
            }
            if (item.getRequestDate() == null) {
                throw new ValidationException("Ngày yêu cầu không được để trống.");
            }
            if (item.getDesiredDeliveryDate() == null || item.getDesiredDeliveryDate().isBefore(item.getRequestDate())) {
                throw new ValidationException("Ngày cần hàng phải sau ngày yêu cầu.");
            }
            if (item.getEstimatedPrice() < 0) {
                throw new ValidationException("Giá ước tính không được âm.");
            }
        }
    }
}
