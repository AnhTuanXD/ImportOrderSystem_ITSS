package com.itss.importorder.service;

import com.itss.importorder.model.Allocation;
import com.itss.importorder.model.DeliveryMeans;
import com.itss.importorder.model.ImportPlan;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.model.RequestStatus;
import com.itss.importorder.model.SiteStatus;
import com.itss.importorder.model.StockRecord;
import com.itss.importorder.repository.DataStore;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanningService {
    private final DataStore store;

    public PlanningService(DataStore store) {
        this.store = store;
    }

    public List<ImportPlan> findAllPlans() {
        return store.getImportPlans();
    }

    public ImportPlan createAutomaticPlan(ImportRequest request, MerchandiseRequest item) {
        List<Candidate> candidates = findCandidates(item);
        int remaining = item.getQuantityOrdered();
        ImportPlan plan = new ImportPlan(nextPlanCode(), request.getRequestCode(), item.getMerchandiseCode(),
                LocalDateTime.now());

        for (Candidate candidate : candidates) {
            if (remaining == 0) {
                break;
            }
            int ordered = Math.min(remaining, candidate.stockRecord.getInStockQuantity());
            plan.getAllocations().add(new Allocation(candidate.site.getSiteCode(), item.getMerchandiseCode(), ordered,
                    item.getUnit(), candidate.deliveryMeans));
            remaining -= ordered;
        }

        if (remaining > 0) {
            throw new ValidationException("Không đủ tồn kho từ các Site đáp ứng ngày nhận mong muốn.");
        }

        try {
            store.saveImportPlan(plan);
            store.getImportPlans().add(plan);
            request.setStatus(RequestStatus.ORDERED);
            store.saveImportRequest(request);
        } catch (SQLException e) {
            System.err.println("Lỗi lưu plan: " + e.getMessage());
            throw new ValidationException("Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return plan;
    }

    public List<StockRecord> findStocksFor(String merchandiseCode) {
        return store.getStockRecords().stream()
                .filter(stock -> stock.getMerchandiseCode().equalsIgnoreCase(merchandiseCode))
                .collect(Collectors.toList());
    }

    public List<CandidatePreview> previewCandidates(MerchandiseRequest item) {
        return findCandidates(item).stream()
                .map(candidate -> new CandidatePreview(candidate.site.getSiteCode(), candidate.site.getName(),
                        candidate.stockRecord.getInStockQuantity(), candidate.stockRecord.getUnit(),
                        candidate.deliveryMeans, candidate.deliveryDays))
                .collect(Collectors.toList());
    }

    private List<Candidate> findCandidates(MerchandiseRequest item) {
        LocalDate today = LocalDate.now();
        List<Candidate> candidates = new ArrayList<>();

        for (StockRecord stock : store.getStockRecords()) {
            if (!stock.getMerchandiseCode().equalsIgnoreCase(item.getMerchandiseCode())
                    || stock.getInStockQuantity() <= 0) {
                continue;
            }
            Optional<ImportSite> siteOpt = store.getImportSites().stream()
                    .filter(site -> site.getSiteCode().equals(stock.getSiteCode()))
                    .filter(site -> site.getStatus() == SiteStatus.ACTIVE)
                    .findFirst();
            if (siteOpt.isEmpty()) {
                continue;
            }
            ImportSite site = siteOpt.get();
            boolean shipCanMeetDeadline = !today.plusDays(site.getDeliveryDaysByShip())
                    .isAfter(item.getDesiredDeliveryDate());
            boolean airCanMeetDeadline = !today.plusDays(site.getDeliveryDaysByAir())
                    .isAfter(item.getDesiredDeliveryDate());
            if (shipCanMeetDeadline) {
                candidates.add(new Candidate(site, stock, DeliveryMeans.SHIP, site.getDeliveryDaysByShip()));
            } else if (airCanMeetDeadline) {
                candidates.add(new Candidate(site, stock, DeliveryMeans.AIR, site.getDeliveryDaysByAir()));
            }
        }

        candidates.sort(Comparator
                .comparing((Candidate candidate) -> candidate.deliveryMeans == DeliveryMeans.SHIP ? 0 : 1)
                .thenComparing((Candidate candidate) -> candidate.stockRecord.getInStockQuantity(), Comparator.reverseOrder())
                .thenComparing(candidate -> candidate.deliveryDays)
                .thenComparing(candidate -> candidate.site.getSiteCode()));
        return candidates;
    }

    private String nextPlanCode() {
        return "PLAN-" + String.format("%03d", store.getImportPlans().size() + 1);
    }

    private static class Candidate {
        private final ImportSite site;
        private final StockRecord stockRecord;
        private final DeliveryMeans deliveryMeans;
        private final int deliveryDays;

        private Candidate(ImportSite site, StockRecord stockRecord, DeliveryMeans deliveryMeans, int deliveryDays) {
            this.site = site;
            this.stockRecord = stockRecord;
            this.deliveryMeans = deliveryMeans;
            this.deliveryDays = deliveryDays;
        }
    }

    public static class CandidatePreview {
        private final String siteCode;
        private final String siteName;
        private final int inStockQuantity;
        private final String unit;
        private final DeliveryMeans deliveryMeans;
        private final int deliveryDays;

        public CandidatePreview(String siteCode, String siteName, int inStockQuantity, String unit,
                DeliveryMeans deliveryMeans, int deliveryDays) {
            this.siteCode = siteCode;
            this.siteName = siteName;
            this.inStockQuantity = inStockQuantity;
            this.unit = unit;
            this.deliveryMeans = deliveryMeans;
            this.deliveryDays = deliveryDays;
        }

        public String getSiteCode() {
            return siteCode;
        }

        public String getSiteName() {
            return siteName;
        }

        public int getInStockQuantity() {
            return inStockQuantity;
        }

        public String getUnit() {
            return unit;
        }

        public DeliveryMeans getDeliveryMeans() {
            return deliveryMeans;
        }

        public int getDeliveryDays() {
            return deliveryDays;
        }
    }
}


