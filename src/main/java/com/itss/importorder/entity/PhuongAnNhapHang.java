package com.itss.importorder.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhuongAnNhapHang {
    private final String planCode;
    private final String requestCode;
    private final String merchandiseCode;
    private final LocalDateTime createdAt;
    private final List<PhanBo> allocations = new ArrayList<>();

    public PhuongAnNhapHang(String planCode, String requestCode, String merchandiseCode, LocalDateTime createdAt) {
        this.planCode = planCode;
        this.requestCode = requestCode;
        this.merchandiseCode = merchandiseCode;
        this.createdAt = createdAt;
    }

    public String getPlanCode() { return planCode; }
    public String getRequestCode() { return requestCode; }
    public String getMerchandiseCode() { return merchandiseCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<PhanBo> getAllocations() { return allocations; }

    public int getTotalQuantity() {
        return allocations.stream().mapToInt(PhanBo::getQuantityOrdered).sum();
    }
}
