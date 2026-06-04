package com.itss.importorder.model;

public class Allocation {
    private final String siteCode;
    private final String merchandiseCode;
    private final int quantityOrdered;
    private final String unit;
    private final DeliveryMeans deliveryMeans;

    public Allocation(String siteCode, String merchandiseCode, int quantityOrdered, String unit,
            DeliveryMeans deliveryMeans) {
        this.siteCode = siteCode;
        this.merchandiseCode = merchandiseCode;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.deliveryMeans = deliveryMeans;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public String getMerchandiseCode() {
        return merchandiseCode;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public String getUnit() {
        return unit;
    }

    public DeliveryMeans getDeliveryMeans() {
        return deliveryMeans;
    }
}

