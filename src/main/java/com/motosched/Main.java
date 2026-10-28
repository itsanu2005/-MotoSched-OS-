package com.motosched;

import com.motosched.repository.*;
import com.motosched.service.*;
import com.motosched.model.ServiceJob;
import com.motosched.db.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final CustomerRepository customers = new CustomerRepository();
    private static final VehicleRepository vehicles = new VehicleRepository();
    private static final ServiceRepository services = new ServiceRepository();
    private static final WorkshopRepository workshop = new WorkshopRepository();
    private static final InventoryRepository inventory = new InventoryRepository();
    private static final InvoiceRepository invoices = new InvoiceRepository();
    private static final SchedulingService scheduler = new SchedulingService();

    public static void main(String[] args) {
        try (Connection ignored = Database.getConnection()) {
            System.out.println("Connected to MotoSched OS MySQL database.");
        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL: " + e.getMessage());
            System.out.println("Check MySQL is running, run sql/schema.sql, and check README.md credentials.");
            return;
        }

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addCustomer();
                    case "2" -> customers.list();
                    case "3" -> addVehicle();
                    case "4" -> vehicles.list();
                    case "5" -> addService();
                    case "6" -> services.listAll();
                    case "7" -> runScheduling();
                    case "8" -> addMechanic();
                    case "9" -> addBay();
                    case "10" -> workshop.listResources();
                    case "11" -> addPart();
                    case "12" -> receiveStock();
                    case "13" -> usePart();
                    case "14" -> assignResources();
                    case "15" -> completeService();
                    case "16" -> createInvoice();
                    case "17" -> invoices.list();
                    case "18" -> showHistory();
                    case "0" -> {
                        System.out.println("Thank you for using MotoSched OS.");
                        return;
                    }
                    default -> System.out.println("Invalid menu option.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("Input error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========== MOTOSCHED OS ==========");
        System.out.println("CUSTOMERS AND VEHICLES");
        System.out.println("1. Register customer");
        System.out.println("2. List customers");
        System.out.println("3. Register vehicle");
        System.out.println("4. List vehicles");
        System.out.println("SERVICE REQUESTS AND OS SCHEDULING");
        System.out.println("5. Create service request");
        System.out.println("6. List service requests");
        System.out.println("7. Run scheduler (FCFS/SJF/Priority/RR)");
        System.out.println("WORKSHOP RESOURCES AND INVENTORY");
        System.out.println("8. Add mechanic");
        System.out.println("9. Add service bay");
        System.out.println("10. View mechanics, bays and parts");
        System.out.println("11. Add spare part");
        System.out.println("12. Receive spare-part stock");
        System.out.println("13. Use spare-part stock");
        System.out.println("14. Assign mechanic and bay to a READY service");
        System.out.println("SERVICE COMPLETION AND BILLING");
        System.out.println("15. Complete service and release resources");
        System.out.println("16. Create invoice");
        System.out.println("17. List invoices");
        System.out.println("18. View vehicle service history");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static void addCustomer() throws SQLException {
        String name = read("Customer name: ");
        String phone = read("Phone: ");
        String email = read("Email (optional): ");
        int id = customers.add(name, phone, email);
        System.out.println("Customer created. ID = " + id);
    }

    private static void addVehicle() throws SQLException {
        int customerId = integer("Customer ID: ");
        String registration = read("Registration number: ").toUpperCase();
        String type = read("Type (BIKE/CAR): ").toUpperCase();
        if (!type.equals("BIKE") && !type.equals("CAR")) {
            System.out.println("Type must be BIKE or CAR.");
            return;
        }
        String model = read("Model: ");
        int id = vehicles.add(customerId, registration, type, model);
        System.out.println("Vehicle created. ID = " + id);
    }

    private static void addService() throws SQLException {
        int vehicleId = integer("Vehicle ID: ");
        String type = read("Service type: ");
        int duration = integer("Estimated duration in minutes: ");
        int priority = integer("Priority (1 = highest): ");
        if (duration <= 0 || priority <= 0) {
            System.out.println("Duration and priority must be positive.");
            return;
        }
        int id = services.add(vehicleId, type, duration, priority);
        System.out.println("Service request created. ID = " + id + ", status = READY");
    }

    private static void runScheduling() throws SQLException {
        List<ServiceJob> jobs = services.findReadyJobs();
        System.out.println("1. FCFS  2. SJF  3. Priority  4. Round Robin");
        int choice = integer("Algorithm: ");
        switch (choice) {
            case 1 -> scheduler.displayOrder(jobs, new FcfsStrategy());
            case 2 -> scheduler.displayOrder(jobs, new SjfStrategy());
            case 3 -> scheduler.displayOrder(jobs, new PriorityStrategy());
            case 4 -> runRoundRobin(jobs);
            default -> System.out.println("Invalid algorithm.");
        }
    }

    private static void runRoundRobin(List<ServiceJob> jobs) {
        int quantum = integer("Time quantum in minutes: ");
        RoundRobinStrategy rr = new RoundRobinStrategy();
        List<Integer> order = rr.buildSliceOrder(jobs, quantum);
        if (order.isEmpty()) {
            System.out.println("No READY service requests.");
            return;
        }
        System.out.println("Round Robin time-slice order:");
        for (int i=0; i<order.size(); i++) {
            System.out.print("P" + order.get(i) + (i == order.size()-1 ? "\n" : " -> "));
        }
        System.out.println("This shows time-slice order; use the other schedulers for CT/WT/TAT metrics.");
    }

    private static void addMechanic() throws SQLException {
        String name = read("Mechanic name: ");
        String skill = read("Specialization: ");
        System.out.println("Mechanic created. ID = " + workshop.addMechanic(name, skill));
    }

    private static void addBay() throws SQLException {
        String type = read("Bay type (BIKE/CAR/BOTH): ").toUpperCase();
        if (!type.equals("BIKE") && !type.equals("CAR") && !type.equals("BOTH")) {
            System.out.println("Bay type must be BIKE, CAR, or BOTH.");
            return;
        }
        System.out.println("Bay created. ID = " + workshop.addBay(type));
    }

    private static void addPart() throws SQLException {
        String name = read("Part name: ");
        int quantity = integer("Initial quantity: ");
        double price = decimal("Unit price: ");
        if (quantity < 0 || price < 0) {
            System.out.println("Quantity and price cannot be negative.");
            return;
        }
        workshop.addPart(name, quantity, price);
        System.out.println("Spare part added.");
    }

    private static void receiveStock() throws SQLException {
        int partId = integer("Part ID: ");
        int quantity = integer("Quantity received: ");
        inventory.receiveStock(partId, quantity);
        System.out.println("Stock received.");
    }

    private static void usePart() throws SQLException {
        int partId = integer("Part ID: ");
        int quantity = integer("Quantity to use: ");
        inventory.reserveAndConsume(partId, quantity);
    }

    private static void assignResources() throws SQLException {
        int serviceId = integer("READY service ID: ");
        int mechanicId = integer("Mechanic ID: ");
        int bayId = integer("Bay ID: ");
        workshop.assignService(serviceId, mechanicId, bayId);
    }

    private static void completeService() throws SQLException {
        int serviceId = integer("Service ID: ");
        int duration = integer("Actual duration in minutes: ");
        if (duration < 0) {
            System.out.println("Duration cannot be negative.");
            return;
        }
        services.complete(serviceId, duration);
        workshop.releaseService(serviceId);
        System.out.println("Service marked COMPLETED.");
    }

    private static void createInvoice() throws SQLException {
        int serviceId = integer("Completed service ID: ");
        double labor = decimal("Labor cost: ");
        double parts = decimal("Parts cost: ");
        double tax = decimal("Tax amount: ");
        invoices.create(serviceId, labor, parts, tax);
    }

    private static void showHistory() throws SQLException {
        String registration = read("Vehicle registration number: ");
        services.showHistory(registration);
    }

    private static String read(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static int integer(String prompt) {
        return Integer.parseInt(read(prompt));
    }

    private static double decimal(String prompt) {
        return Double.parseDouble(read(prompt));
    }
}
