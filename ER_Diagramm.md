# ER Diagram — Shuttle Dashboard

**Group 5 · Data Model**

> Shown: all persistent entities of the domain model with their key attributes and relationships.
> `UserRole` is a code enum (no separate table entry); `EMPLOYEE.role` takes one of the four values.

---

```mermaid
erDiagram
EMPLOYEE {
        int id PK
        string name
        int teamId FK
    }
    
    ROLE {
        int id PK
        string roleName
    }

    TEAM {
        int id PK
        string teamName
    }

    EMPLOYEE_ROLE_ASSIGNMENT {
        int employeeId FK
        int roleId FK
    }

    SHUTTLE_PART {
        string partKey PK
        string displayName
    }

    SENSOR_THRESHOLD {
        string partKey FK
        string sensorName
        double min
        double max
        double warningBuffer
    }

    FLIGHT_RECORD {
        int flightId PK
        int flightNumber
    }

    SENSOR_READING {
        int flightId FK
        string partKey FK
        string sensorName
        double value
    }

    REPAIR_TASK {
        string partKey FK
        string sensorName
        string status
        string action
        string requiredItemName
        boolean done
    }

    INVENTORY_ITEM {
        int id PK
        string name
        string part
        int quantity
        string status
        string description
    }

    LOGISTICS_ORDER {
        int orderNumber PK
        string partName
        int quantity
        int orderedById FK
        string reason
        string orderDate
        string status
        int approvedById FK
        string approvalDate
        string deliveryDate
    }

    MAINTENANCE_TICKET {
        int id PK
        string date
        string part FK
        string sensor
        string oldStatus
        string action
        string technician
    }

    ROUTINE_TASK {
        int id PK
        string name
        string shuttlePart FK
        int estimatedMinutes
        int assignedEmployeeId FK
        boolean done
        string completedAt
    }

    SCHEDULE_DAY {
        int dayNumber PK
        string label
    }

    SCHEDULE_ENTRY {
        string time
        string task
        string category
        int assignedEmployeeId FK
        string shuttlePart FK
    }

    TREND_RESULT {
        string partKey FK
        string sensorName
        double trendPerFlight
        string direction
        string recommendation
        int flightsUntilLimit
    }

    SHUTTLE_PART ||--o{ SENSOR_THRESHOLD    : "hat"
    SHUTTLE_PART ||--o{ REPAIR_TASK         : "braucht"
    SHUTTLE_PART ||--o{ MAINTENANCE_TICKET  : "dokumentiert"
    SHUTTLE_PART ||--o{ ROUTINE_TASK        : "erfordert"
    SHUTTLE_PART ||--o{ SCHEDULE_ENTRY      : "geplant"
    SHUTTLE_PART ||--o{ TREND_RESULT        : "analysiert"
    SHUTTLE_PART ||--o{ SENSOR_READING      : "liefert"

    FLIGHT_RECORD ||--o{ SENSOR_READING     : "speichert"

    INVENTORY_ITEM ||--o{ REPAIR_TASK       : "verbraucht"

    EMPLOYEE ||--o{ ROUTINE_TASK            : "führt aus"
    EMPLOYEE ||--o{ LOGISTICS_ORDER         : "bestellt"
    EMPLOYEE ||--o{ LOGISTICS_ORDER         : "genehmigt"
    EMPLOYEE ||--o{ SCHEDULE_ENTRY          : "arbeitet"
    
    EMPLOYEE ||--o{ EMPLOYEE_ROLE_ASSIGNMENT : "besetzt"
    ROLE ||--o{ EMPLOYEE_ROLE_ASSIGNMENT : "definiert"
    
    TEAM ||--o{ EMPLOYEE                    : "enthält"
    SCHEDULE_DAY ||--o{ SCHEDULE_ENTRY      : "plant"