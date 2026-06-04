package com.itss.importorder.entity;

public enum TrangThaiYeuCau {
    SENT("Đã gửi"),
    PLANNING("Đã lập phương án"),
    ORDERED("Đã đặt hàng"),
    RECEIVED("Đã nhập kho"),
    CANCELLED("Đã hủy");

    private final String displayName;

    TrangThaiYeuCau(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
