USE motosched;

INSERT INTO customers(customer_name, phone, email)
VALUES ('Anurag Yadav', '9876500001', 'anurag@example.com');

INSERT INTO vehicles(customer_id, registration_number, vehicle_type, model)
SELECT customer_id, 'UK07AB1234', 'BIKE', 'Pulsar 150'
FROM customers WHERE phone='9876500001';

INSERT INTO service_requests(vehicle_id, service_type, estimated_duration, priority, status)
SELECT vehicle_id, 'Engine oil change', 30, 2, 'READY'
FROM vehicles WHERE registration_number='UK07AB1234';

INSERT INTO mechanics(mechanic_name, specialization)
VALUES ('Ravi Kumar', 'Bike servicing');

INSERT INTO service_bays(bay_type) VALUES ('BIKE');

INSERT INTO spare_parts(part_name, quantity_in_stock, unit_price)
VALUES ('Engine oil', 20, 450.00);

COMMIT;
