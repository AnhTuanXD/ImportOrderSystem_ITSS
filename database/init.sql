-- PostgreSQL Database Initialization Script
-- Database: importorder_db

-- Tạo bảng Users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng ImportRequest
CREATE TABLE IF NOT EXISTS import_requests (
    id SERIAL PRIMARY KEY,
    request_code VARCHAR(50) UNIQUE NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng ImportSite
CREATE TABLE IF NOT EXISTS import_sites (
    id SERIAL PRIMARY KEY,
    site_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    delivery_days_by_ship INTEGER,
    delivery_days_by_air INTEGER,
    other_information TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng StockRecord
CREATE TABLE IF NOT EXISTS stock_records (
    id SERIAL PRIMARY KEY,
    site_code VARCHAR(50) NOT NULL,
    merchandise_code VARCHAR(50) NOT NULL,
    in_stock_quantity INTEGER NOT NULL,
    unit VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(site_code, merchandise_code)
);

-- Tạo bảng ImportPlan
CREATE TABLE IF NOT EXISTS import_plans (
    id SERIAL PRIMARY KEY,
    plan_code VARCHAR(50) UNIQUE NOT NULL,
    request_code VARCHAR(50) NOT NULL,
    merchandise_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng PlanAllocation (phân bổ site cho từng phương án)
CREATE TABLE IF NOT EXISTS plan_allocations (
    id SERIAL PRIMARY KEY,
    plan_code VARCHAR(50) NOT NULL REFERENCES import_plans(plan_code) ON DELETE CASCADE,
    site_code VARCHAR(50) NOT NULL,
    merchandise_code VARCHAR(50) NOT NULL,
    quantity_ordered INTEGER NOT NULL,
    unit VARCHAR(50),
    delivery_means VARCHAR(20) NOT NULL
);

-- Tạo bảng WarehouseReport
CREATE TABLE IF NOT EXISTS warehouse_reports (
    id SERIAL PRIMARY KEY,
    report_code VARCHAR(50) UNIQUE NOT NULL,
    request_code VARCHAR(50) NOT NULL,
    checker VARCHAR(50) NOT NULL,
    checked_at TIMESTAMP NOT NULL,
    result VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng MerchandiseRequest (chi tiết hàng hóa)
CREATE TABLE IF NOT EXISTS merchandise_requests (
    id SERIAL PRIMARY KEY,
    import_request_id INTEGER NOT NULL REFERENCES import_requests(id) ON DELETE CASCADE,
    merchandise_code VARCHAR(50),
    merchandise_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    quantity_ordered INTEGER NOT NULL,
    unit VARCHAR(50),
    stock_level INTEGER,
    request_date DATE,
    desired_delivery_date DATE,
    supplier VARCHAR(255),
    estimated_price DECIMAL(10, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo chỉ mục
CREATE INDEX IF NOT EXISTS idx_import_requests_created_date ON import_requests(created_date);
CREATE INDEX IF NOT EXISTS idx_import_sites_site_code ON import_sites(site_code);
CREATE INDEX IF NOT EXISTS idx_stock_records_site_code ON stock_records(site_code);
CREATE INDEX IF NOT EXISTS idx_import_plans_request_code ON import_plans(request_code);
CREATE INDEX IF NOT EXISTS idx_warehouse_reports_request_code ON warehouse_reports(request_code);
CREATE INDEX IF NOT EXISTS idx_merchandise_requests_import_request_id ON merchandise_requests(import_request_id);
