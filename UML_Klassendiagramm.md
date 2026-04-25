# UML Klassendiagramm — Shuttle Dashboard

**Gruppe 5 · Architekturübersicht**

> Dargestellt: öffentliche, protected und package-private Methoden/Attribute.
> Private Members werden nur gezeigt, wenn sie für das Architekturverständnis unbedingt nötig sind.

---

## Controller-Schicht

```mermaid
classDiagram
    class Main {
        +main(args: String[])$
    }

    class App {
        +start(stage: Stage)
    }

    class BaseController {
        <<abstract>>
        #getNavigationButton()* Button
        #setupBackButton(btn: Button)
        #loadView(fxml: String)
    }

    class LoginController {
        +initialize()
        +handleLogin()
    }

    class MainController {
        +initialize()
        +updateDashboard()
    }

    class MissionControlController {
        +initialize()
    }

    class TechnicianController {
        +initialize()
    }

    class HistoryController {
        +initialize()
    }

    class InventoryController {
        +initialize()
    }

    class LogisticsController {
        +initialize()
    }

    class ScheduleController {
        +initialize()
    }

    class StaffController {
        +initialize()
    }

    class SensorPanelController {
        +update(data: ShuttleData, thresholds: Map)
    }

    class SchedulePanelController {
        +update()
    }

    class PredictivePanelController {
        +update()
    }

    class InventoryStatusPanelController {
        +update(displayName: String)
    }

    class AiChatController {
        +toggle()
        +handleInput()
    }

    class RoleAccessController {
        ~applyRestrictions(allNavButtons: List)
    }

    class RoleConfig {
        <<utility>>
        ~RESTRICTED_BUTTONS: Map$
    }

    class RoutineTableHelper {
        <<utility>>
        ~setup(table, colName, colDone, colTime)$
        ~setup(table, colName, colEst, colDone, colTime)$
    }

    Main --> App

    BaseController <|-- MainController
    BaseController <|-- MissionControlController
    BaseController <|-- TechnicianController
    BaseController <|-- HistoryController
    BaseController <|-- InventoryController
    BaseController <|-- LogisticsController
    BaseController <|-- ScheduleController
    BaseController <|-- StaffController

    MainController --> SensorPanelController : erstellt
    MainController --> SchedulePanelController : erstellt
    MainController --> PredictivePanelController : erstellt
    MainController --> AiChatController : erstellt
    MainController --> RoleAccessController : erstellt
    RoleAccessController ..> RoleConfig : liest
    MissionControlController --> InventoryStatusPanelController : erstellt
    MissionControlController ..> RoutineTableHelper : nutzt
    StaffController ..> RoutineTableHelper : nutzt
```

---

## Service-Schicht — Interfaces und Implementierungen

```mermaid
classDiagram
    class SensorEvaluator {
        <<interface>>
        +evaluate(value: double, threshold: SensorThreshold) SensorStatus
    }

    class ISensorDataService {
        <<interface>>
        +loadSensorData() ShuttleData
        +loadThresholds() Map
    }

    class IEmployeeService {
        <<interface>>
        +getAllEmployees() List~Employee~
        +getById(id: String) Optional~Employee~
        +getByRole(role: String) List~Employee~
    }

    class IInventoryService {
        <<interface>>
        +loadInventory() List~InventoryItem~
        +saveInventory(items: List)
    }

    class IScheduleService {
        <<interface>>
        +loadSchedule() TakeoverSchedule
    }

    class IFlightHistoryService {
        <<interface>>
        +loadFlightHistory() FlightHistory
    }

    class IPredictiveAnalysisService {
        <<interface>>
        +analyzeAll() Map~String, List~TrendResult~~
    }

    class IPartApproval {
        <<interface>>
        +isPartApproved(partKey: String) boolean
        +canApprove(partKey: String) boolean
        +approve(partKey: String, chiefName: String)
    }

    class IRepairAccess {
        <<interface>>
        +getRepairs(partKey: String) List~RepairTask~
        +allRepairsDone(partKey: String) boolean
        +isTechnicianDone(partKey: String) boolean
        +markTechnicianDone(partKey: String, techName: String)
        +generateRepairs(data, thresholds, evaluator)
    }

    class ITakeoverProgress {
        <<interface>>
        +getProgress() double
        +isTakeoverComplete() boolean
        +getLastActivity() String
    }

    class IWorkerRegistry {
        <<interface>>
        +registerWorker(partKey, name, role)
        +getTechnicianName(partKey: String) String
        +getSecurityChiefName(partKey: String) String
    }

    class IPhaseTracker {
        <<interface>>
        +getAppPhase() AppPhase
        +getRemainingSeconds() int
        +getScheduleStatus() ScheduleStatus
        +beginLanding()
        +setOperational()
        +reset()
    }

    ISensorDataService --|> SensorEvaluator

    class TakeoverState {
        <<Singleton>>
        +getInstance()$ TakeoverState
        +getDisplayName(partKey: String)$ String
        +PART_KEYS: String[]$
        +reset()
    }

    class EmployeeService {
        <<Singleton>>
        +getInstance()$ EmployeeService
        +getAllEmployees() List~Employee~
        +getById(id: String) Optional~Employee~
        +getByTeam(team: String) List~Employee~
    }

    class SensorDataService {
        <<Singleton>>
        +getInstance()$ SensorDataService
        +loadSensorData() ShuttleData
        +loadThresholds() Map
        +evaluate(value: double, t: SensorThreshold) SensorStatus
    }

    class InventoryService {
        <<Singleton>>
        +getInstance()$ InventoryService
        +loadInventory() List~InventoryItem~
        +saveInventory(items: List)
    }

    class ScheduleService {
        <<Singleton>>
        +getInstance()$ ScheduleService
        +loadSchedule() TakeoverSchedule
    }

    class FlightHistoryService {
        <<Singleton>>
        +getInstance()$ FlightHistoryService
        +loadFlightHistory() FlightHistory
    }

    class PredictiveAnalysisService {
        +analyzeAll() Map~String, List~TrendResult~~
    }

    class SessionState {
        <<Singleton>>
        +getInstance()$ SessionState
        +setCurrentUser(e: Employee)
        +getCurrentUser() Employee
        +getCurrentRole() String
        +isLoggedIn() boolean
    }

    class RoutineTaskStore {
        <<Singleton>>
        +getInstance()$ RoutineTaskStore
        +getAllTasks() List~RoutineTask~
        +getTasksForEmployee(empId: String) List~RoutineTask~
        +getTasksForPart(part: String) List~RoutineTask~
        +reset()
    }

    class PhaseTimerService {
        <<Singleton>>
        +getInstance()$ PhaseTimerService
        +start(seconds: int, onTick: Consumer, onComplete: Runnable)
        +stop()
    }

    class AbstractStore~T~ {
        <<abstract>>
        +getAll() List~T~
        +clear()
    }

    class OrderStore {
        <<Singleton>>
        +getInstance()$ OrderStore
        +createOrder(partName, qty, orderedBy, reason) LogisticsOrder
        +getOrders() List~LogisticsOrder~
    }

    class TicketStore {
        <<Singleton>>
        +getInstance()$ TicketStore
        +getTickets() List~MaintenanceTicket~
        +saveTickets(tickets: List)
    }

    TakeoverState ..|> IWorkerRegistry
    TakeoverState ..|> IRepairAccess
    TakeoverState ..|> IPartApproval
    TakeoverState ..|> ITakeoverProgress
    TakeoverState ..|> IPhaseTracker
    EmployeeService ..|> IEmployeeService
    SensorDataService ..|> ISensorDataService
    InventoryService ..|> IInventoryService
    ScheduleService ..|> IScheduleService
    FlightHistoryService ..|> IFlightHistoryService
    PredictiveAnalysisService ..|> IPredictiveAnalysisService
    AbstractStore <|-- OrderStore
    AbstractStore <|-- TicketStore

    PredictiveAnalysisService --> IFlightHistoryService
    PredictiveAnalysisService --> ISensorDataService

    class AiAdvisorService {
        <<utility>>
        +getResponse(message: String)$ String
    }

    class OrderApprovalService {
        <<utility>>
        +approve(order, invService, empService, onDelivered)$
        +reject(order, empService)$
    }

    class RepairInventoryService {
        <<utility>>
        +initTaskStatuses(tasks, inventoryService)$
        +placeOrder(task, inventoryService, onArrived)$
        +deductInventory(task, inventoryService, onDeducted)$
    }

    class MaintenanceHistoryService {
        <<utility>>
        +logRepairs(partKey, repairAccess, workerRegistry)$
    }

    class RepairPlanningService {
        <<utility>>
        +plan(data, thresholds, evaluator, inventoryService)$ Map
    }
```

---

## Controller → Service Abhängigkeiten (Dependency Inversion)

```mermaid
classDiagram
    class LoginController
    class MainController
    class MissionControlController
    class LogisticsController
    class ScheduleController
    class StaffController
    class TechnicianController
    class SensorPanelController
    class SchedulePanelController
    class PredictivePanelController
    class InventoryStatusPanelController

    class IEmployeeService { <<interface>> }
    class ISensorDataService { <<interface>> }
    class IInventoryService { <<interface>> }
    class IScheduleService { <<interface>> }
    class IPredictiveAnalysisService { <<interface>> }
    class IPartApproval { <<interface>> }
    class IRepairAccess { <<interface>> }
    class ITakeoverProgress { <<interface>> }
    class IWorkerRegistry { <<interface>> }
    class IPhaseTracker { <<interface>> }
    class SensorEvaluator { <<interface>> }

    class SessionState { <<Singleton>> }
    class TakeoverState { <<Singleton>> }
    class EmployeeService { <<Singleton>> }
    class SensorDataService { <<Singleton>> }
    class InventoryService { <<Singleton>> }
    class ScheduleService { <<Singleton>> }
    class PredictiveAnalysisService

    TakeoverState ..|> IPartApproval
    TakeoverState ..|> IRepairAccess
    TakeoverState ..|> ITakeoverProgress
    TakeoverState ..|> IWorkerRegistry
    TakeoverState ..|> IPhaseTracker
    EmployeeService ..|> IEmployeeService
    SensorDataService ..|> ISensorDataService
    SensorDataService ..|> SensorEvaluator
    InventoryService ..|> IInventoryService
    ScheduleService ..|> IScheduleService
    PredictiveAnalysisService ..|> IPredictiveAnalysisService

    LoginController --> IEmployeeService
    LoginController --> SessionState
    MainController --> ISensorDataService
    MainController --> IScheduleService
    MainController --> IPhaseTracker
    MainController --> IPartApproval
    MainController --> ITakeoverProgress
    MainController --> IWorkerRegistry
    MainController --> IRepairAccess
    MainController --> IPredictiveAnalysisService
    MainController --> SessionState
    MissionControlController --> IWorkerRegistry
    MissionControlController --> IRepairAccess
    MissionControlController --> IPartApproval
    MissionControlController --> IInventoryService
    MissionControlController --> IScheduleService
    LogisticsController --> IInventoryService
    LogisticsController --> IEmployeeService
    StaffController --> IWorkerRegistry
    StaffController --> IRepairAccess
    ScheduleController --> IScheduleService
    TechnicianController --> ISensorDataService
    SensorPanelController --> SensorEvaluator
    SensorPanelController --> IPartApproval
    SchedulePanelController --> IScheduleService
    SchedulePanelController --> IEmployeeService
    PredictivePanelController --> IPredictiveAnalysisService
    InventoryStatusPanelController --> IInventoryService
```

---

## Util-Schicht

```mermaid
classDiagram
    class ColoredTableCell~T~ {
        +ColoredTableCell(colorMapper: Function~String, String~)
        +updateItem(item: String, empty: boolean)
    }

    class EmployeeStringConverter {
        +INSTANCE: EmployeeStringConverter$
        +toString(e: Employee) String
        +fromString(s: String) Employee
    }

    class EmployeeListCell {
        +updateItem(emp: Employee, empty: boolean)
    }

    class EmployeeComboHelper {
        <<utility>>
        +setup(combo: ComboBox~Employee~)$
    }

    class ShuttleDataHelper {
        <<utility>>
        +evaluateSensor(sensorName, value, thresholds, evaluator)$ SensorStatus
    }

    class StatusColors {
        <<utility>>
        +forSensorStatus(status: SensorStatus)$ String
        +forStockStatus(status: StockStatus)$ String
        +forOrderStatus(status: OrderStatus)$ String
        +forScheduleCategoryFg(category: String)$ String
        +forTrendUrgency(flights: int)$ String
    }

    class Styles {
        <<utility>>
        +TABLE_DARK: String$
        +PART_BTN_NORMAL: String$
        +PART_BTN_SELECTED: String$
        +ACCENT_SUCCESS: String$
        +ACCENT_WARNING: String$
        +ACCENT_ERROR: String$
        +label12(color: String)$ String
        +label13(color: String)$ String
    }

    class Dialogs {
        <<utility>>
        +showError(title, header, msg)$
        +showInfo(title, msg)$
        +showWarning(msg)$
    }

    EmployeeListCell ..> EmployeeStringConverter
    EmployeeComboHelper ..> EmployeeStringConverter
    ShuttleDataHelper ..> SensorEvaluator
```
