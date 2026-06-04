package com.itss.importorder;

import com.itss.importorder.model.ImportPlan;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import com.itss.importorder.service.PlanningService;
import com.itss.importorder.service.ValidationException;

public class TestRunner {
    public static void main(String[] args) {
        testPlanningSuccess();
        testPlanningFailsWhenStockIsMissing();
        System.out.println("All service tests passed.");
    }

    private static void testPlanningSuccess() {
        DataStore store = SampleDataFactory.create();
        PlanningService service = new PlanningService(store);
        ImportRequest request = store.getImportRequests().get(0);
        MerchandiseRequest item = request.getItems().get(0);
        ImportPlan plan = service.createAutomaticPlan(request, item);
        assertTrue(plan.getTotalQuantity() == item.getQuantityOrdered(), "Plan must satisfy requested quantity");
        assertTrue(!plan.getAllocations().isEmpty(), "Plan must have allocations");
    }

    private static void testPlanningFailsWhenStockIsMissing() {
        DataStore store = SampleDataFactory.create();
        PlanningService service = new PlanningService(store);
        ImportRequest request = store.getImportRequests().get(1);
        MerchandiseRequest item = request.getItems().get(0);
        MerchandiseRequest impossible = new MerchandiseRequest("SSD-2T", "SSD 2TB NVMe", "Storage", 
                9999, "pcs", 0, java.time.LocalDate.now(), item.getDesiredDeliveryDate(), 
                "Test Supplier", 100.00, "Test note");
        boolean failed = false;
        try {
            service.createAutomaticPlan(request, impossible);
        } catch (ValidationException expected) {
            failed = true;
        }
        assertTrue(failed, "Planning must fail if stock is insufficient");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

