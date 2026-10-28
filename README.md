# MotoSched OS — Modular Console Project (Java + MySQL)

A console-based workshop manager demonstrating Operating Systems scheduling and DBMS persistence.

## Modules
- `db/Database.java`: JDBC connection configuration.
- `model/ServiceJob.java`: OS process/job data model.
- `repository/`: SQL persistence for customers, vehicles, services, workshop resources, inventory, and invoices.
- `service/`: scheduling strategies and scheduling output.
- `Main.java`: console menu that connects the modules.
- `sql/schema.sql`: MySQL tables, constraints, and indexes.
- `sql/sample_data.sql`: optional demo data.
- `sql/reports.sql`: example reports.

## Requirements
- JDK 17+
- Maven
- MySQL Server 8+
- VS Code with Extension Pack for Java (recommended)

## Setup
1. Start MySQL Server.
2. Open MySQL Workbench and run `sql/schema.sql`.
3. Optional: run `sql/sample_data.sql` once for demo records. If you run it repeatedly, unique phone/registration constraints will reject duplicates.
4. Set database environment variables before launching:
   - `MOTOSCHED_DB_URL` (default `jdbc:mysql://localhost:3306/motosched`)
   - `MOTOSCHED_DB_USER` (default `root`)
   - `MOTOSCHED_DB_PASSWORD` (default empty)
5. Open this folder in VS Code.
6. In VS Code terminal, run:
   ```bash
   mvn clean compile
   mvn exec:java
   ```

Example macOS/Linux environment setup for one terminal session:
```bash
export MOTOSCHED_DB_USER="root"
export MOTOSCHED_DB_PASSWORD="your_mysql_password"
mvn exec:java
```
On Windows PowerShell:
```powershell
$env:MOTOSCHED_DB_USER="root"
$env:MOTOSCHED_DB_PASSWORD="your_mysql_password"
mvn exec:java
```
Never commit passwords to source control.

## Menu walkthrough
1. Register a customer and note the generated ID.
2. Register a vehicle using that customer ID.
3. Create service requests using the generated vehicle ID.
4. Run FCFS, SJF, Priority or Round Robin to compare queue order.
5. Add a mechanic and bay; use option 14 to assign them to a READY service.
6. Mark the service complete with option 15, which releases an active assignment.
7. Add parts and receive/use stock with options 11–13.
8. Create an invoice for a service with option 16.
9. View reports and vehicle history.

## Scheduling assumptions
- FCFS, non-preemptive SJF, and non-preemptive Priority are implemented as selectable strategies.
- Priority number 1 is the highest.
- The queue order is simulated in Java using READY jobs retrieved from MySQL.
- Round Robin prints the time-slice order for a supplied time quantum.
- In this educational version, scheduling output does not itself allocate resources or change request status. Resource assignment is a separate menu operation.
- The database stores arrival timestamps; the scheduling module uses the order in which READY jobs are retrieved as an arrival-order approximation.

## Scope note
This is a complete runnable educational console application for the features listed above. It is not a production workshop platform. It does not yet implement login/permissions, deadline-aware carry-forward, aging, automatic inventory deduction per service, or robust multi-user race prevention across all workflows.
