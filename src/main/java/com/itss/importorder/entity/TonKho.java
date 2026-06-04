package com.itss.importorder.entity;

public class TonKho {
    private final String siteCode;
    private final String merchandiseCode;
    private String merchandiseName;
    private int inStockQuantity;
    private String unit;

    public TonKho(String siteCode, String merchandiseCode, String merchandiseName, int inStockQuantity, String unit) {
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.merchandiseName = merchandiseName;
        this.inStockQuantity = inStockQuantity;
        this.unit = unit;
    }

    public String getSiteCode() { return siteCode; }
    public String getMerchandiseCode() { return merchandiseCode; }

    public String getMerchandiseName() { return merchandiseName; }
    public void setMerchandiseName(String merchandiseName) { this.merchandiseName = merchandiseName; }

    public int getInStockQuantity() { return inStockQuantity; }
    public void setInStockQuantity(int inStockQuantity) { this.inStockQuantity = inStockQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
