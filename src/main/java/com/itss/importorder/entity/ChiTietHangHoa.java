package com.itss.importorder.entity;

import java.time.LocalDate;

public class ChiTietHangHoa {
    private String merchandiseCode;
    private String merchandiseName;
    private String category;
    private int quantityOrdered;
    private String unit;
    private int stockLevel;
    private LocalDate requestDate;
    private LocalDate desiredDeliveryDate;
    private String supplier;
    private double estimatedPrice;
    private String notes;

    public ChiTietHangHoa(String merchandiseCode, String merchandiseName, String category,
                          int quantityOrdered, String unit, int stockLevel, LocalDate requestDate,
                          LocalDate desiredDeliveryDate, String supplier, double estimatedPrice, String notes) {
        this.merchandiseCode = merchandiseCode;
        this.merchandiseName = merchandiseName;
        this.category = category;
        this.quantityOrdered = quantityOrdered;
        this.unit = unit;
        this.stockLevel = stockLevel;
        this.requestDate = requestDate;
        this.desiredDeliveryDate = desiredDeliveryDate;
        this.supplier = supplier;
        this.estimatedPrice = estimatedPrice;
        this.notes = notes;
    }

    public String getMerchandiseCode() { return merchandiseCode; }
    public void setMerchandiseCode(String merchandiseCode) { this.merchandiseCode = merchandiseCode; }

    public String getMerchandiseName() { return merchandiseName; }
    public void setMerchandiseName(String merchandiseName) { this.merchandiseName = merchandiseName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public LocalDate getDesiredDeliveryDate() { return desiredDeliveryDate; }
    public void setDesiredDeliveryDate(LocalDate desiredDeliveryDate) { this.desiredDeliveryDate = desiredDeliveryDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
