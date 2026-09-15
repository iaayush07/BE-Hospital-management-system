# Hospital Management System — Backend

A RESTful Spring Boot backend for managing patients, doctors, and appointments in a hospital. Built as the API layer for a React SPA frontend.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation |
| Boilerplate reduction | Lombok |
| Build | Maven |

---

## Architecture

The application follows a strict three-layer architecture — each layer has one job and depends only on the layer below it.

```
HTTP Request
     │
     ▼
┌─────────────┐
│  Controller │  Reads request params/body, delegates, returns ResponseEntity
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │  Business logic, domain rules, orchestration
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │  Spring Data JPA — database I/O only
└─────────────┘
```

Controllers never contain business logic. Services never build HTTP responses. Repositories are interfaces — no implementation code.

---

## Project Structure

```
src/main/java/com/onerivet/
├── config/
│   ├── CorsConfig.java          — CORS whitelist for the React frontend
│   └── DataInitializer.java     — Seed data on first startup
├── controller/
│   ├── PatientController.java
│   ├── DoctorController.java
│   └── AppointmentController.java
├── service/
│   ├── PatientService.java
│   ├── DoctorService.java
│   └── AppointmentService.java
├── repository/
│   ├── PatientRepository.java
│   ├── DoctorRepository.java
│   └── AppointmentRepository.java
├── specification/
│   ├── PatientSpec.java         — JPA Specifications for patient filtering
│   ├── DoctorSpec.java          — JPA Specifications for doctor filtering
│   └── AppointmentSpec.java     — JPA Specifications for appointment filtering
├── model/
│   ├── Patient.java
│   ├── Doctor.java
│   ├── Appointment.java
│   ├── AppointmentStatus.java   — Enum: SCHEDULED | COMPLETED | CANCELLED
│   └── Specialization.java      — Enum: CARDIOLOGY | NEUROLOGY | ORTHOPEDICS | PEDIATRICS | DERMATOLOGY | GENERAL
├── dto/
│   ├── PatientRequest.java
│   ├── PatientResponse.java
│   ├── DoctorRequest.java
│   ├── DoctorResponse.java
│   ├── AppointmentRequest.java
│   ├── AppointmentResponse.java
│   └── PageResponse.java        — Generic paginated response wrapper
└── exception/
    ├── NotFoundException.java
    ├── SlotUnavailableException.java
    └── GlobalExceptionHandler.java
```

---

## Domain Model

```
Patient ──────────────────────────┐
  id, name, age, gender, phone    │ ManyToOne
                                  ▼
                           Appointment
Doctor ───────────────────────────┤
  id, name, specialization, phone │ ManyToOne
                                  │
                            id, slot (LocalDateTime),
                            status (SCHEDULED | COMPLETED | CANCELLED)
```

- A patient can have many appointments.
- A doctor can have many appointments.
- An appointment belongs to exactly one patient and one doctor.
- No two non-cancelled appointments for the same doctor can share the same slot (slot uniqueness enforced in the service layer).

---

## API Reference

### Patients — `/api/patients`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/patients` | Register a new patient |
| `GET` | `/api/patients/{id}` | Get patient by ID |
| `GET` | `/api/patients` | List all patients (filterable) |

**Filter params for `GET /api/patients`:**

| Param | Type | Behaviour |
|---|---|---|
| `name` | String | Case-insensitive partial match |
| `phone` | String | Partial match |
| `gender` | String | Exact match (`Male` / `Female` / `Other`) |

---

### Doctors — `/api/doctors`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/doctors` | Register a new doctor |
| `GET` | `/api/doctors/{id}` | Get doctor by ID |
| `GET` | `/api/doctors/{id}/schedule` | Get a doctor's upcoming non-cancelled appointments |
| `GET` | `/api/doctors` | List all doctors (filterable) |

**Filter params for `GET /api/doctors`:**

| Param | Type | Behaviour |
|---|---|---|
| `name` | String | Case-insensitive partial match |
| `specialization` | String | Exact enum match (e.g. `CARDIOLOGY`) |

---

### Appointments — `/api/appointments`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/appointments` | Book an appointment |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancel an appointment |
| `PATCH` | `/api/appointments/{id}/complete` | Mark an appointment as completed |
| `GET` | `/api/appointments` | List appointments (filterable, paginated) |

**Filter + pagination params for `GET /api/appointments`:**

| Param | Type | Behaviour |
|---|---|---|
| `name` | String | Case-insensitive match on patient OR doctor name |
| `status` | String | Exact enum match (`SCHEDULED` / `COMPLETED` / `CANCELLED`) |
| `page` | int | Zero-based page index (default: 0) |
| `size` | int | Page size (default: 20) |
| `sort` | String | Sort field and direction (default: `slot,asc`) |

**Paginated response shape:**
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3,
  "last": false
}
```

---

## Design Decisions

### DTOs at every boundary
Request and response objects are separate from JPA entities. This prevents accidental exposure of internal fields (e.g. database IDs on nested relations), allows independent evolution of the API contract and the schema, and makes validation annotations clean — they live on the request DTO, not the entity.

### JPA Specifications for filtering
All list endpoints support optional, combinable query-parameter filters. Rather than writing one query method per filter combination (which explodes combinatorially), each filter is expressed as a `Specification<T>` — a composable predicate. At query time, only the non-null specs are AND-composed into a single query. Omitting all params returns the full unfiltered list, preserving backward compatibility.

```java
Specification<Patient> spec = Specification
    .where(PatientSpec.hasName(name))
    .and(PatientSpec.hasPhone(phone))
    .and(PatientSpec.hasGender(gender));
```

Each spec method returns `null` when its param is blank, and `Specification.where(null)` is a no-op — so no conditional branching is needed in the service.

### Pagination on appointments only
Patients and doctors return plain lists; the dataset is bounded and manageable. Appointments can grow unbounded over time (every visit creates one), so the appointment list endpoint uses Spring Data's `Pageable` and returns a generic `PageResponse<T>` record. The default page size is 20, sorted by slot ascending.

### Slot conflict check in the service layer
Booking validates that the requested slot is not already taken by a non-cancelled appointment for the same doctor:

```java
boolean slotTaken = appointmentRepository
    .existsByDoctorIdAndSlotAndStatusNot(doctorId, slot, CANCELLED);
```

This is a deliberate service-layer check rather than a database unique constraint, because cancelled appointments must be allowed to reuse a slot — a constraint can't express that condition without a partial index, which is less portable.

### Enum stored as String
`AppointmentStatus` and `Specialization` are persisted with `@Enumerated(EnumType.STRING)`. This makes the database readable without the application, prevents silent bugs when enum ordinal positions shift, and makes migrations safe when new values are added.

### CORS scoped to the dev frontend origin
CORS is explicitly allowed only for `http://localhost:5173` (the Vite dev server). This is intentional — a wildcard origin would silently allow any web page to call the API with the user's credentials.

### Idempotent seed data
`DataInitializer` checks `patientRepo.count()` before seeding, so restarting the application does not duplicate data. It also loads existing records from the database when seed is skipped, so subsequent appointment seeding can reference real entity IDs.

---

## Getting Started

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL running locally on port `5432`

### Database setup

```sql
CREATE DATABASE hospital_db;
```

Update credentials in `src/main/resources/application.yaml` if your PostgreSQL username/password differ from the defaults.

### Run

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`. On first startup, `DataInitializer` seeds 6 patients, 5 doctors, and 23 appointments (a mix of scheduled, completed, and cancelled) so the frontend has realistic data immediately.

### Health check

```
GET http://localhost:8080/actuator/health
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Initial Spring Boot scaffold |
| `develop` | Integration branch — features merge here first |
| `feature/patient-module` | Current development branch — all domain features |
