package com.itss.importorder.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class YeuCauNhapHang {
    private final String requestCode;
    private String createdBy;
    private LocalDate createdDate;
    private TrangThaiYeuCau status;
    private final List<ChiTietHangHoa> items = new ArrayList<>();

    public YeuCauNhapHang(String requestCode, String createdBy, LocalDate createdDate, TrangThaiYeuCau status) {
        this.requestCode = requestCode;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.status = status;
    }

    public String getRequestCode() { return requestCode; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public TrangThaiYeuCau getStatus() { return status; }
    public void setStatus(TrangThaiYeuCau status) { this.status = status; }

    public List<ChiTietHangHoa> getItems() { return items; }

    public int getTotalQuantity() {
        return items.stream().mapToInt(ChiTietHangHoa::getQuantityOrdered).sum();
    }
}
