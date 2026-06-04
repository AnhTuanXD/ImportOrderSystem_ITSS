package com.itss.importorder.entity;

public enum TrangThaiYeuCau {
    DRAFT("Nhập"),
    SENT("Đã gửi"),
    PLANNING("Đang lập phương án"),
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
