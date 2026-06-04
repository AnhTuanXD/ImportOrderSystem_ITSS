package com.itss.importorder.entity;

public enum VaiTro {
    ADMIN("Quản trị hệ thống"),
    SALES("Bộ phận bán hàng"),
    OVERSEAS_ORDER("Bộ phận đặt hàng quốc tế"),
    IMPORT_SITE("Site nhập khẩu"),
    WAREHOUSE("Bộ phận quản lý kho");

    private final String displayName;

    VaiTro(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
