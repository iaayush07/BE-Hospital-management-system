# Hospital Management System — React Frontend Design

**Date:** 2026-09-09
**Status:** Approved
**Backend:** `http://localhost:8080` (Spring Boot, already running)

---

## 1. Project Location & Toolchain

| Decision | Choice | Reason |
|---|---|---|
| Location | `C:\Java\hospital-management-system-fe\` | Sibling repo, matches existing TMG-FE pattern |
| Scaffolder | Vite + React + TypeScript | Fastest dev server, modern standard for SPAs |
| UI | shadcn/ui + Tailwind CSS | Full code ownership, Radix accessibility, flexible styling |
| HTTP / Cache | TanStack Query + Axios | Automatic loading/error/cache states, typed interceptors |
| Forms | React Hook Form + Zod | Uncontrolled inputs (perf), schema-driven validation |
| Routing | React Router v6 | De facto SPA standard, nested routes |

---

## 2. Pages & Routes

| Route | Page | API endpoints used |
|---|---|---|
| `/` | Dashboard | — (summary of registered patients/doctors) |
| `/patients` | Patient list + register form | `POST /api/patients` |
| `/patients/:id` | Patient detail | `GET /api/patients/{id}` |
| `/doctors` | Doctor list + register form | `POST /api/doctors` |
| `/doctors/:id` | Doctor detail + schedule tab | `GET /api/doctors/{id}`, `GET /api/doctors/{id}/schedule` |
| `/appointments` | Appointment list + book form + actions | `POST /api/appointments`, `PATCH /api/appointments/{id}/cancel`, `PATCH /api/appointments/{id}/complete` |

---

## 3. Folder Structure

```
hospital-management-system-fe/
├── src/
│   ├── api/
│   │   ├── axios.ts            # axios instance with baseURL + error interceptor
│   │   ├── patients.ts         # typed functions: registerPatient, getPatient
│   │   ├── doctors.ts          # typed functions: registerDoctor, getDoctor, getDoctorSchedule
│   │   └── appointments.ts     # typed functions: bookAppointment, cancelAppointment, completeAppointment
│   ├── components/
│   │   ├── ui/                 # shadcn-generated components (Button, Input, Table, etc.)
│   │   ├── Layout.tsx          # top nav + sidebar + <Outlet />
│   │   ├── Navbar.tsx          # top bar with app title and nav links
│   │   └── ErrorMessage.tsx    # reusable error display component
│   ├── features/
│   │   ├── patients/
│   │   │   ├── PatientList.tsx     # table + inline register button
│   │   │   ├── PatientForm.tsx     # RHF form, used in modal on PatientList
│   │   │   └── PatientDetail.tsx   # patient info card
│   │   ├── doctors/
│   │   │   ├── DoctorList.tsx
│   │   │   ├── DoctorForm.tsx
│   │   │   ├── DoctorDetail.tsx
│   │   │   └── DoctorSchedule.tsx  # table of upcoming appointments
│   │   └── appointments/
│   │       ├── AppointmentList.tsx  # table with cancel/complete action buttons
│   │       └── AppointmentForm.tsx  # RHF form: patientId, doctorId, slot (datetime)
│   ├── lib/
│   │   └── schemas.ts          # all Zod schemas (mirrors backend DTOs)
│   ├── types/
│   │   └── index.ts            # TS types inferred from Zod schemas
│   ├── App.tsx                 # React Router route tree
│   └── main.tsx                # Vite entry, QueryClientProvider wrap
├── index.html
├── tailwind.config.ts
├── tsconfig.json
└── vite.config.ts
```

---

## 4. Data Layer

### Axios instance (`src/api/axios.ts`)
- `baseURL`: `http://localhost:8080/api`
- Response interceptor: maps backend `400` / `404` / `409` error bodies to a typed `ApiError`

### TanStack Query
- `QueryClient` provided at root in `main.tsx`
- Query keys follow `['patients', id]` pattern
- Mutations call `queryClient.invalidateQueries` on success to keep lists fresh

### Zod schemas (`src/lib/schemas.ts`)
Mirror the backend DTOs exactly:
```ts
patientRequestSchema    // name, age, gender, phone
doctorRequestSchema     // name, specialization (enum), phone
appointmentRequestSchema // patientId, doctorId, slot (ISO datetime, future)
```
TypeScript types for responses (`PatientResponse`, `DoctorResponse`, `AppointmentResponse`) are defined in `src/types/index.ts`.

---

## 5. Error Handling

- `400` validation errors: display field-level messages from backend (mirroring client-side Zod errors)
- `404`: show `ErrorMessage` component with "Not found" text
- `409` slot conflict: toast/inline alert "This slot is already booked"
- Network failures: TanStack Query `error` state shown via `ErrorMessage`

---

## 6. State Management

No global state library needed. All server state is owned by TanStack Query. Local UI state (modal open/close, selected row) stays in component `useState`.

---

## 7. What Is Out of Scope

- Authentication / login (no auth on the backend)
- Pagination (API returns unbounded lists; acceptable for a demo)
- Tests (first iteration; can be added in a follow-up)
