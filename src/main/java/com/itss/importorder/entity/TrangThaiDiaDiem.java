package com.itss.importorder.entity;

public enum TrangThaiDiaDiem {
    ACTIVE("Đang hoạt động"),
    DISABLED("Vô hiệu hóa");

    private final String displayName;

    TrangThaiDiaDiem(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
