USE motosched;

-- All services with customer and vehicle details
SELECT c.customer_name, v.registration_number, sr.service_id,
       sr.service_type, sr.arrival_time, sr.estimated_duration,
       sr.priority, sr.status
FROM customers c
JOIN vehicles v ON v.customer_id=c.customer_id
JOIN service_requests sr ON sr.vehicle_id=v.vehicle_id
ORDER BY sr.arrival_time;

-- Ready queue (FCFS)
SELECT service_id, service_type, arrival_time
FROM service_requests WHERE status='READY'
ORDER BY arrival_time, service_id;

-- Service count by status
SELECT status, COUNT(*) AS total_jobs
FROM service_requests GROUP BY status;

-- Inventory
SELECT part_id, part_name, quantity_in_stock, unit_price
FROM spare_parts ORDER BY part_name;

-- Invoices
SELECT invoice_id, service_id, labor_cost, parts_cost, tax, total_amount
FROM invoices ORDER BY generated_at DESC;
