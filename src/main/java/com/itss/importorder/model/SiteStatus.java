package com.itss.importorder.model;

public enum SiteStatus {
    ACTIVE("Đang hoạt động"),
    DISABLED("Vô hiệu hóa");

    private final String displayName;

    SiteStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

