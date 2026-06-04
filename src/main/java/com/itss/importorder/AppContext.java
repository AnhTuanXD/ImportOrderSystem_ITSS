package com.itss.importorder;

import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import com.itss.importorder.service.AuthService;
import com.itss.importorder.service.ImportRequestService;
import com.itss.importorder.service.PlanningService;
import com.itss.importorder.service.SiteService;
import com.itss.importorder.service.WarehouseService;

public class AppContext {
    private final DataStore store = SampleDataFactory.create();
    private final AuthService authService = new AuthService(store);
    private final ImportRequestService importRequestService = new ImportRequestService(store);
    private final SiteService siteService = new SiteService(store);
    private final PlanningService planningService = new PlanningService(store);
    private final WarehouseService warehouseService = new WarehouseService(store);

    public AuthService getAuthService() {
        return authService;
    }

    public ImportRequestService getImportRequestService() {
        return importRequestService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public PlanningService getPlanningService() {
        return planningService;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }
}

