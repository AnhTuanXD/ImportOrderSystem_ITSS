package com.itss.importorder.model;

public enum Role {
    SALES("Bộ phận bán hàng"),
    OVERSEAS_ORDER("Bộ phận đặt hàng quốc tế"),
    IMPORT_SITE("Site nhập khẩu"),
    WAREHOUSE("Bộ phận quản lý kho");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

