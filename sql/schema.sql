CREATE DATABASE IF NOT EXISTS motosched;
USE motosched;

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type ENUM('BIKE','CAR') NOT NULL,
    model VARCHAR(100) NOT NULL,
    CONSTRAINT fk_vehicle_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS service_requests (
    service_id INT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id INT NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    arrival_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estimated_duration INT NOT NULL,
    actual_duration INT NULL,
    priority INT NOT NULL DEFAULT 3,
    deadline DATETIME NULL,
    status ENUM('READY','RUNNING','WAITING_FOR_MECHANIC','WAITING_FOR_BAY',
                'WAITING_FOR_PARTS','COMPLETED','CANCELLED','CARRY_FORWARD')
           NOT NULL DEFAULT 'READY',
    CONSTRAINT fk_request_vehicle FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(vehicle_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_duration CHECK (estimated_duration > 0),
    CONSTRAINT chk_actual_duration CHECK (actual_duration IS NULL OR actual_duration >= 0),
    CONSTRAINT chk_priority CHECK (priority > 0)
);

CREATE TABLE IF NOT EXISTS service_tasks (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    estimated_duration INT NOT NULL,
    required_skill VARCHAR(100),
    status ENUM('READY','RUNNING','COMPLETED') NOT NULL DEFAULT 'READY',
    CONSTRAINT fk_task_request FOREIGN KEY (service_id)
        REFERENCES service_requests(service_id) ON DELETE CASCADE,
    CONSTRAINT chk_task_duration CHECK (estimated_duration > 0)
);

CREATE TABLE IF NOT EXISTS mechanics (
    mechanic_id INT PRIMARY KEY AUTO_INCREMENT,
    mechanic_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS service_bays (
    bay_id INT PRIMARY KEY AUTO_INCREMENT,
    bay_type ENUM('BIKE','CAR','BOTH') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS service_assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT NOT NULL,
    mechanic_id INT NOT NULL,
    bay_id INT NOT NULL,
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME NULL,
    assignment_status ENUM('ACTIVE','COMPLETED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (service_id) REFERENCES service_requests(service_id),
    FOREIGN KEY (mechanic_id) REFERENCES mechanics(mechanic_id),
    FOREIGN KEY (bay_id) REFERENCES service_bays(bay_id),
    CHECK (end_time IS NULL OR end_time >= start_time)
);

CREATE TABLE IF NOT EXISTS spare_parts (
    part_id INT PRIMARY KEY AUTO_INCREMENT,
    part_name VARCHAR(100) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_stock CHECK (quantity_in_stock >= 0),
    CONSTRAINT chk_price CHECK (unit_price >= 0)
);

CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    service_id INT NOT NULL UNIQUE,
    labor_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
    parts_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (service_id) REFERENCES service_requests(service_id),
    CHECK (labor_cost >= 0 AND parts_cost >= 0 AND tax >= 0 AND total_amount >= 0)
);

CREATE INDEX idx_request_queue ON service_requests(status, arrival_time);
CREATE INDEX idx_vehicle_history ON service_requests(vehicle_id, arrival_time);
