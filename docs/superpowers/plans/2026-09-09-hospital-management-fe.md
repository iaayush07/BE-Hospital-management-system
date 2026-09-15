# Hospital Management System — React Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a React SPA at `C:\Java\hospital-management-system-fe\` that consumes the hospital management Spring Boot API on `http://localhost:8080`.

**Architecture:** Feature-based folder structure with a shared API layer. All server state managed by TanStack Query. Forms validated client-side with Zod then submitted via Axios. React Router v6 handles navigation between Patient, Doctor, and Appointment pages.

**Tech Stack:** Vite 6, React 19, TypeScript 5, Tailwind CSS v4 (`@tailwindcss/vite`), shadcn/ui, TanStack Query v5, Axios, React Hook Form v7, Zod v3, React Router v6.

## Global Constraints

- All API calls target `http://localhost:8080/api` — Spring Boot must be running on that port
- TypeScript strict mode — no `any`, all API responses fully typed
- All Zod schemas are the single source of truth for both validation and request types
- TanStack Query owns all server state — no `useState` for remote data
- Path alias `@/` maps to `src/` throughout

---

## File Map

### Backend changes (Task 1)
- Create: `src/main/java/com/example/hospital_management_system/config/CorsConfig.java`
- Modify: `src/main/resources/application.yaml` — add Jackson datetime config
- Modify: `src/main/java/com/example/hospital_management_system/service/PatientService.java` — add `getAll()`
- Modify: `src/main/java/com/example/hospital_management_system/service/DoctorService.java` — add `getAll()`
- Modify: `src/main/java/com/example/hospital_management_system/service/AppointmentService.java` — add `getAll()`
- Modify: `src/main/java/com/example/hospital_management_system/controller/PatientController.java` — add `GET /`
- Modify: `src/main/java/com/example/hospital_management_system/controller/DoctorController.java` — add `GET /`
- Modify: `src/main/java/com/example/hospital_management_system/controller/AppointmentController.java` — add `GET /`

### Frontend files (Tasks 2–8)
```
hospital-management-system-fe/
  src/
    api/
      axios.ts              — Axios instance, baseURL=http://localhost:8080/api, error interceptor
      patients.ts           — getAllPatients, getPatient, registerPatient
      doctors.ts            — getAllDoctors, getDoctor, getDoctorSchedule, registerDoctor
      appointments.ts       — getAllAppointments, bookAppointment, cancelAppointment, completeAppointment
    components/
      ui/                   — shadcn-generated (button, input, label, table, dialog, badge, select, card)
      Layout.tsx            — Navbar + <Outlet />
      Navbar.tsx            — nav links with active highlight
      ErrorMessage.tsx      — red bordered error box
    features/
      dashboard/
        Dashboard.tsx       — static welcome page
      patients/
        PatientForm.tsx     — RHF + Zod, calls registerPatient mutation
        PatientList.tsx     — table + "Register Patient" dialog trigger
        PatientDetail.tsx   — card fetched by URL param id
      doctors/
        DoctorForm.tsx      — RHF + Zod + shadcn Select for specialization
        DoctorList.tsx      — table + "Register Doctor" dialog trigger
        DoctorDetail.tsx    — card + embedded DoctorSchedule
        DoctorSchedule.tsx  — appointment table fetched via getDoctorSchedule
      appointments/
        AppointmentForm.tsx — RHF + Zod, patientId/doctorId inputs + datetime-local slot
        AppointmentList.tsx — table with cancel/complete action buttons per row
    lib/
      schemas.ts            — Zod schemas + inferred PatientRequest/DoctorRequest/AppointmentRequest types
    types/
      index.ts              — PatientResponse, DoctorResponse, AppointmentResponse, Specialization, AppointmentStatus
    App.tsx                 — BrowserRouter + route tree
    main.tsx                — QueryClientProvider wrapping App
```

---

## Task 1: Prepare the Spring Boot backend

**Files:**
- Create: `src/main/java/com/example/hospital_management_system/config/CorsConfig.java`
- Modify: `src/main/resources/application.yaml`
- Modify: `src/main/java/com/example/hospital_management_system/service/PatientService.java`
- Modify: `src/main/java/com/example/hospital_management_system/service/DoctorService.java`
- Modify: `src/main/java/com/example/hospital_management_system/service/AppointmentService.java`
- Modify: `src/main/java/com/example/hospital_management_system/controller/PatientController.java`
- Modify: `src/main/java/com/example/hospital_management_system/controller/DoctorController.java`
- Modify: `src/main/java/com/example/hospital_management_system/controller/AppointmentController.java`

**Interfaces:**
- Produces:
  - `GET /api/patients` → `List<PatientResponse>`
  - `GET /api/doctors` → `List<DoctorResponse>`
  - `GET /api/appointments` → `List<AppointmentResponse>`
  - CORS allowed for `http://localhost:5173`
  - `slot` field serialized as ISO-8601 string (not array)

- [ ] **Step 1: Add Jackson datetime config to application.yaml**

Add to `src/main/resources/application.yaml` under the `spring:` block:
```yaml
  jackson:
    serialization:
      write-dates-as-timestamps: false
```
Full file after edit:
```yaml
spring:
  application:
    name: hospital-management-system

  datasource:
    url: jdbc:postgresql://localhost:5432/hospital_db
    username: postgres
    password: Admin#123
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  jackson:
    serialization:
      write-dates-as-timestamps: false

server:
  port: 8080
```

- [ ] **Step 2: Create CorsConfig**

Create `src/main/java/com/example/hospital_management_system/config/CorsConfig.java`:
```java
package com.example.hospital_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PATCH", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}
```

- [ ] **Step 3: Add `getAll()` to PatientService**

In `PatientService.java`, add after `getById`:
```java
public List<PatientResponse> getAll() {
    return patientRepository.findAll().stream().map(this::toResponse).toList();
}
```
Add `import java.util.List;` at the top if not present.

- [ ] **Step 4: Add `GET /` to PatientController**

In `PatientController.java`, add:
```java
@GetMapping
public ResponseEntity<List<PatientResponse>> getAll() {
    return ResponseEntity.ok(patientService.getAll());
}
```
Add `import java.util.List;` if not present.

- [ ] **Step 5: Add `getAll()` to DoctorService**

In `DoctorService.java`, add after `getById`:
```java
public List<DoctorResponse> getAll() {
    return doctorRepository.findAll().stream().map(this::toResponse).toList();
}
```
Add `import java.util.List;` at the top if not present.

- [ ] **Step 6: Add `GET /` to DoctorController**

In `DoctorController.java`, add:
```java
@GetMapping
public ResponseEntity<List<DoctorResponse>> getAll() {
    return ResponseEntity.ok(doctorService.getAll());
}
```
Add `import java.util.List;` if not present.

- [ ] **Step 7: Add `getAll()` to AppointmentService**

In `AppointmentService.java`, add after `getDoctorSchedule`:
```java
public List<AppointmentResponse> getAll() {
    return appointmentRepository.findAll().stream().map(this::toResponse).toList();
}
```
`List` import already present.

- [ ] **Step 8: Add `GET /` to AppointmentController**

In `AppointmentController.java`, add:
```java
@GetMapping
public ResponseEntity<List<AppointmentResponse>> getAll() {
    return ResponseEntity.ok(appointmentService.getAll());
}
```

- [ ] **Step 9: Restart the app and verify**

Stop the running Spring Boot process, then:
```bash
cd /c/Java/hospital-management-system
mvn spring-boot:run -q &
```
Wait ~15 seconds, then:
```bash
curl -s -H "Origin: http://localhost:5173" http://localhost:8080/api/patients
curl -s http://localhost:8080/api/doctors
curl -s http://localhost:8080/api/appointments
```
Expected: `[]` from each (200 OK, empty arrays).
Also verify CORS header present:
```bash
curl -s -I -H "Origin: http://localhost:5173" http://localhost:8080/api/patients | grep -i access-control
```
Expected: `Access-Control-Allow-Origin: http://localhost:5173`

- [ ] **Step 10: Commit**

```bash
git add src/main/java/com/example/hospital_management_system/config/CorsConfig.java \
        src/main/resources/application.yaml \
        src/main/java/com/example/hospital_management_system/service/PatientService.java \
        src/main/java/com/example/hospital_management_system/service/DoctorService.java \
        src/main/java/com/example/hospital_management_system/service/AppointmentService.java \
        src/main/java/com/example/hospital_management_system/controller/PatientController.java \
        src/main/java/com/example/hospital_management_system/controller/DoctorController.java \
        src/main/java/com/example/hospital_management_system/controller/AppointmentController.java
git commit -m "feat: add list endpoints, CORS config and ISO datetime serialization"
```

---

## Task 2: Scaffold the React project

**Files:**
- Create: `C:\Java\hospital-management-system-fe\` (entire project)

**Interfaces:**
- Produces: Vite dev server running at `http://localhost:5173` with Tailwind + shadcn/ui wired up

- [ ] **Step 1: Create Vite project**

```bash
cd /c/Java
npm create vite@latest hospital-management-system-fe -- --template react-ts
cd hospital-management-system-fe
npm install
```

- [ ] **Step 2: Install runtime dependencies**

```bash
npm install @tanstack/react-query axios react-hook-form @hookform/resolvers zod react-router-dom
```

- [ ] **Step 3: Install dev dependencies**

```bash
npm install -D tailwindcss @tailwindcss/vite @types/node
```

- [ ] **Step 4: Replace vite.config.ts**

```ts
import path from 'path';
import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
});
```

- [ ] **Step 5: Add Tailwind import to src/index.css**

Replace the full content of `src/index.css` with:
```css
@import "tailwindcss";
```

- [ ] **Step 6: Add path alias to tsconfig.json**

Open `tsconfig.json` and add inside `compilerOptions`:
```json
"baseUrl": ".",
"paths": {
  "@/*": ["./src/*"]
}
```

- [ ] **Step 7: Initialise shadcn/ui**

```bash
npx shadcn@latest init -d
```
Accept all defaults (New York style, neutral base color, CSS variables: yes).

- [ ] **Step 8: Install required shadcn components**

```bash
npx shadcn@latest add button input label table dialog badge select card
```

- [ ] **Step 9: Clean up Vite boilerplate**

Delete `src/App.css` and `src/assets/react.svg`.

Replace `src/App.tsx` with:
```tsx
export default function App() {
  return <div className="p-4">Hospital Management System</div>;
}
```

Replace `src/main.tsx` with:
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
```

- [ ] **Step 10: Verify dev server**

```bash
npm run dev
```
Open `http://localhost:5173`. Expected: page shows "Hospital Management System" in plain text, no console errors.

- [ ] **Step 11: Commit**

```bash
git init
git add .
git commit -m "chore: scaffold Vite + React + TypeScript + Tailwind v4 + shadcn/ui"
```

---

## Task 3: Types, Zod schemas, and Axios instance

**Files:**
- Create: `src/types/index.ts`
- Create: `src/lib/schemas.ts`
- Create: `src/api/axios.ts`

**Interfaces:**
- Produces:
  - Types: `Specialization`, `AppointmentStatus`, `PatientResponse`, `DoctorResponse`, `AppointmentResponse`
  - Schemas + request types: `patientRequestSchema / PatientRequest`, `doctorRequestSchema / DoctorRequest`, `appointmentRequestSchema / AppointmentRequest`, `SPECIALIZATIONS`
  - `api`: configured Axios instance

- [ ] **Step 1: Create response types**

Create `src/types/index.ts`:
```ts
export type Specialization =
  | 'CARDIOLOGY'
  | 'NEUROLOGY'
  | 'ORTHOPEDICS'
  | 'PEDIATRICS'
  | 'DERMATOLOGY'
  | 'GENERAL';

export type AppointmentStatus = 'SCHEDULED' | 'CANCELLED' | 'COMPLETED';

export interface PatientResponse {
  id: number;
  name: string;
  age: number;
  gender: string;
  phone: string;
}

export interface DoctorResponse {
  id: number;
  name: string;
  specialization: Specialization;
  phone: string;
}

export interface AppointmentResponse {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  slot: string;       // ISO-8601 string from backend
  status: AppointmentStatus;
}
```

- [ ] **Step 2: Create Zod schemas**

Create `src/lib/schemas.ts`:
```ts
import { z } from 'zod';

export const SPECIALIZATIONS = [
  'CARDIOLOGY',
  'NEUROLOGY',
  'ORTHOPEDICS',
  'PEDIATRICS',
  'DERMATOLOGY',
  'GENERAL',
] as const;

export const patientRequestSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  age: z.coerce.number().positive('Age must be positive'),
  gender: z.string().min(1, 'Gender is required'),
  phone: z.string().min(1, 'Phone is required'),
});
export type PatientRequest = z.infer<typeof patientRequestSchema>;

export const doctorRequestSchema = z.object({
  name: z.string().min(1, 'Name is required'),
  specialization: z.enum(SPECIALIZATIONS, { required_error: 'Specialization is required' }),
  phone: z.string().min(1, 'Phone is required'),
});
export type DoctorRequest = z.infer<typeof doctorRequestSchema>;

export const appointmentRequestSchema = z.object({
  patientId: z.coerce
    .number({ invalid_type_error: 'Patient ID is required' })
    .positive('Patient ID must be positive'),
  doctorId: z.coerce
    .number({ invalid_type_error: 'Doctor ID is required' })
    .positive('Doctor ID must be positive'),
  slot: z
    .string()
    .min(1, 'Slot is required')
    .refine((val) => new Date(val) > new Date(), {
      message: 'Slot must be a future date and time',
    }),
});
export type AppointmentRequest = z.infer<typeof appointmentRequestSchema>;
```

- [ ] **Step 3: Create Axios instance**

Create `src/api/axios.ts`:
```ts
import axios from 'axios';

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message: string =
      error.response?.data?.message ??
      error.response?.data?.errors?.[0]?.defaultMessage ??
      'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);
```

- [ ] **Step 4: Verify TypeScript compiles**

```bash
npm run build 2>&1 | head -30
```
Expected: build completes with no errors in the new files.

- [ ] **Step 5: Commit**

```bash
git add src/types/index.ts src/lib/schemas.ts src/api/axios.ts
git commit -m "feat(api): add response types, Zod schemas and Axios instance"
```

---

## Task 4: API service functions

**Files:**
- Create: `src/api/patients.ts`
- Create: `src/api/doctors.ts`
- Create: `src/api/appointments.ts`

**Interfaces:**
- Consumes: `api` from `src/api/axios.ts`; types from `src/types/index.ts`; request types from `src/lib/schemas.ts`
- Produces:
  - `getAllPatients(): Promise<PatientResponse[]>`
  - `getPatient(id: number): Promise<PatientResponse>`
  - `registerPatient(data: PatientRequest): Promise<PatientResponse>`
  - `getAllDoctors(): Promise<DoctorResponse[]>`
  - `getDoctor(id: number): Promise<DoctorResponse>`
  - `getDoctorSchedule(id: number): Promise<AppointmentResponse[]>`
  - `registerDoctor(data: DoctorRequest): Promise<DoctorResponse>`
  - `getAllAppointments(): Promise<AppointmentResponse[]>`
  - `bookAppointment(data: AppointmentRequest): Promise<AppointmentResponse>`
  - `cancelAppointment(id: number): Promise<AppointmentResponse>`
  - `completeAppointment(id: number): Promise<AppointmentResponse>`

- [ ] **Step 1: Create patients API**

Create `src/api/patients.ts`:
```ts
import type { PatientResponse } from '@/types';
import type { PatientRequest } from '@/lib/schemas';
import { api } from './axios';

export const getAllPatients = (): Promise<PatientResponse[]> =>
  api.get('/patients').then((r) => r.data);

export const getPatient = (id: number): Promise<PatientResponse> =>
  api.get(`/patients/${id}`).then((r) => r.data);

export const registerPatient = (data: PatientRequest): Promise<PatientResponse> =>
  api.post('/patients', data).then((r) => r.data);
```

- [ ] **Step 2: Create doctors API**

Create `src/api/doctors.ts`:
```ts
import type { DoctorResponse, AppointmentResponse } from '@/types';
import type { DoctorRequest } from '@/lib/schemas';
import { api } from './axios';

export const getAllDoctors = (): Promise<DoctorResponse[]> =>
  api.get('/doctors').then((r) => r.data);

export const getDoctor = (id: number): Promise<DoctorResponse> =>
  api.get(`/doctors/${id}`).then((r) => r.data);

export const getDoctorSchedule = (id: number): Promise<AppointmentResponse[]> =>
  api.get(`/doctors/${id}/schedule`).then((r) => r.data);

export const registerDoctor = (data: DoctorRequest): Promise<DoctorResponse> =>
  api.post('/doctors', data).then((r) => r.data);
```

- [ ] **Step 3: Create appointments API**

Create `src/api/appointments.ts`:
```ts
import type { AppointmentResponse } from '@/types';
import type { AppointmentRequest } from '@/lib/schemas';
import { api } from './axios';

export const getAllAppointments = (): Promise<AppointmentResponse[]> =>
  api.get('/appointments').then((r) => r.data);

export const bookAppointment = (data: AppointmentRequest): Promise<AppointmentResponse> =>
  api.post('/appointments', data).then((r) => r.data);

export const cancelAppointment = (id: number): Promise<AppointmentResponse> =>
  api.patch(`/appointments/${id}/cancel`).then((r) => r.data);

export const completeAppointment = (id: number): Promise<AppointmentResponse> =>
  api.patch(`/appointments/${id}/complete`).then((r) => r.data);
```

- [ ] **Step 4: Verify TypeScript compiles**

```bash
npm run build 2>&1 | head -30
```
Expected: no type errors in the three new files.

- [ ] **Step 5: Commit**

```bash
git add src/api/patients.ts src/api/doctors.ts src/api/appointments.ts
git commit -m "feat(api): add typed service functions for patients, doctors and appointments"
```

---

## Task 5: App shell — routing, layout, and providers

**Files:**
- Create: `src/components/ErrorMessage.tsx`
- Create: `src/components/Navbar.tsx`
- Create: `src/components/Layout.tsx`
- Create: `src/features/dashboard/Dashboard.tsx`
- Create: `src/features/patients/PatientList.tsx` (placeholder)
- Create: `src/features/patients/PatientDetail.tsx` (placeholder)
- Create: `src/features/doctors/DoctorList.tsx` (placeholder)
- Create: `src/features/doctors/DoctorDetail.tsx` (placeholder)
- Create: `src/features/appointments/AppointmentList.tsx` (placeholder)
- Modify: `src/App.tsx`
- Modify: `src/main.tsx`

**Interfaces:**
- Produces: working nav between all routes, Navbar active-link highlight, `QueryClient` available to all components

- [ ] **Step 1: Create ErrorMessage**

Create `src/components/ErrorMessage.tsx`:
```tsx
interface ErrorMessageProps {
  message: string;
}

export function ErrorMessage({ message }: ErrorMessageProps) {
  return (
    <div className="rounded-md bg-red-50 border border-red-200 p-4 text-sm text-red-700">
      {message}
    </div>
  );
}
```

- [ ] **Step 2: Create Navbar**

Create `src/components/Navbar.tsx`:
```tsx
import { Link, useLocation } from 'react-router-dom';

const NAV_LINKS = [
  { to: '/patients', label: 'Patients' },
  { to: '/doctors', label: 'Doctors' },
  { to: '/appointments', label: 'Appointments' },
];

export function Navbar() {
  const { pathname } = useLocation();
  return (
    <nav className="border-b bg-white px-6 py-4 flex items-center gap-6 shadow-sm">
      <Link to="/" className="font-bold text-lg text-primary mr-4">
        Hospital MS
      </Link>
      {NAV_LINKS.map((link) => (
        <Link
          key={link.to}
          to={link.to}
          className={`text-sm font-medium transition-colors hover:text-primary ${
            pathname.startsWith(link.to) ? 'text-primary' : 'text-muted-foreground'
          }`}
        >
          {link.label}
        </Link>
      ))}
    </nav>
  );
}
```

- [ ] **Step 3: Create Layout**

Create `src/components/Layout.tsx`:
```tsx
import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

export function Layout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="container mx-auto px-6 py-8 max-w-5xl">
        <Outlet />
      </main>
    </div>
  );
}
```

- [ ] **Step 4: Create Dashboard and placeholder feature pages**

Create `src/features/dashboard/Dashboard.tsx`:
```tsx
export function Dashboard() {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-2">Dashboard</h1>
      <p className="text-muted-foreground">Welcome to the Hospital Management System.</p>
    </div>
  );
}
```

Create `src/features/patients/PatientList.tsx`:
```tsx
export function PatientList() {
  return <div>Patient List — coming soon</div>;
}
```

Create `src/features/patients/PatientDetail.tsx`:
```tsx
export function PatientDetail() {
  return <div>Patient Detail — coming soon</div>;
}
```

Create `src/features/doctors/DoctorList.tsx`:
```tsx
export function DoctorList() {
  return <div>Doctor List — coming soon</div>;
}
```

Create `src/features/doctors/DoctorDetail.tsx`:
```tsx
export function DoctorDetail() {
  return <div>Doctor Detail — coming soon</div>;
}
```

Create `src/features/appointments/AppointmentList.tsx`:
```tsx
export function AppointmentList() {
  return <div>Appointment List — coming soon</div>;
}
```

- [ ] **Step 5: Wire up App.tsx**

Replace `src/App.tsx`:
```tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from '@/components/Layout';
import { Dashboard } from '@/features/dashboard/Dashboard';
import { PatientList } from '@/features/patients/PatientList';
import { PatientDetail } from '@/features/patients/PatientDetail';
import { DoctorList } from '@/features/doctors/DoctorList';
import { DoctorDetail } from '@/features/doctors/DoctorDetail';
import { AppointmentList } from '@/features/appointments/AppointmentList';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="patients" element={<PatientList />} />
          <Route path="patients/:id" element={<PatientDetail />} />
          <Route path="doctors" element={<DoctorList />} />
          <Route path="doctors/:id" element={<DoctorDetail />} />
          <Route path="appointments" element={<AppointmentList />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
```

- [ ] **Step 6: Add QueryClientProvider to main.tsx**

Replace `src/main.tsx`:
```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { retry: 1, staleTime: 30_000 },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);
```

- [ ] **Step 7: Verify in browser**

```bash
npm run dev
```
Open `http://localhost:5173`. Check:
- Navbar shows "Hospital MS" + three links
- Clicking each link shows the placeholder text
- Active link is darker/primary colour than inactive ones
- No console errors

- [ ] **Step 8: Commit**

```bash
git add src/components/ src/features/ src/App.tsx src/main.tsx
git commit -m "feat(shell): add routing, layout, Navbar and placeholder pages"
```

---

## Task 6: Patient feature

**Files:**
- Create: `src/features/patients/PatientForm.tsx`
- Modify: `src/features/patients/PatientList.tsx`
- Modify: `src/features/patients/PatientDetail.tsx`

**Interfaces:**
- Consumes: `getAllPatients`, `getPatient`, `registerPatient` from `src/api/patients.ts`; `patientRequestSchema`, `PatientRequest` from `src/lib/schemas.ts`; `PatientResponse` from `src/types/index.ts`; shadcn `Button`, `Input`, `Label`, `Table*`, `Dialog*`, `Card*`; `ErrorMessage` from `src/components/ErrorMessage.tsx`
- Produces: `/patients` — table of all patients + "Register Patient" dialog; `/patients/:id` — patient detail card with back button

- [ ] **Step 1: Create PatientForm**

Create `src/features/patients/PatientForm.tsx`:
```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patientRequestSchema, type PatientRequest } from '@/lib/schemas';
import { registerPatient } from '@/api/patients';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ErrorMessage } from '@/components/ErrorMessage';

interface PatientFormProps {
  onSuccess: () => void;
}

export function PatientForm({ onSuccess }: PatientFormProps) {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PatientRequest>({ resolver: zodResolver(patientRequestSchema) });

  const { mutate, isPending, error } = useMutation({
    mutationFn: registerPatient,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      onSuccess();
    },
  });

  return (
    <form onSubmit={handleSubmit((data) => mutate(data))} className="space-y-4">
      {error && <ErrorMessage message={(error as Error).message} />}
      <div className="space-y-1">
        <Label htmlFor="name">Name</Label>
        <Input id="name" {...register('name')} />
        {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
      </div>
      <div className="space-y-1">
        <Label htmlFor="age">Age</Label>
        <Input id="age" type="number" {...register('age')} />
        {errors.age && <p className="text-sm text-red-600">{errors.age.message}</p>}
      </div>
      <div className="space-y-1">
        <Label htmlFor="gender">Gender</Label>
        <Input id="gender" {...register('gender')} placeholder="e.g. Male, Female" />
        {errors.gender && <p className="text-sm text-red-600">{errors.gender.message}</p>}
      </div>
      <div className="space-y-1">
        <Label htmlFor="phone">Phone</Label>
        <Input id="phone" {...register('phone')} />
        {errors.phone && <p className="text-sm text-red-600">{errors.phone.message}</p>}
      </div>
      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? 'Registering...' : 'Register Patient'}
      </Button>
    </form>
  );
}
```

- [ ] **Step 2: Implement PatientList**

Replace `src/features/patients/PatientList.tsx`:
```tsx
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getAllPatients } from '@/api/patients';
import { PatientForm } from './PatientForm';
import { Button } from '@/components/ui/button';
import { ErrorMessage } from '@/components/ErrorMessage';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table';

export function PatientList() {
  const [open, setOpen] = useState(false);
  const { data: patients, isLoading, error } = useQuery({
    queryKey: ['patients'],
    queryFn: getAllPatients,
  });

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Patients</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>Register Patient</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Register New Patient</DialogTitle>
            </DialogHeader>
            <PatientForm onSuccess={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      {isLoading && <p className="text-muted-foreground">Loading...</p>}
      {error && <ErrorMessage message={(error as Error).message} />}
      {patients?.length === 0 && (
        <p className="text-muted-foreground">No patients registered yet.</p>
      )}
      {patients && patients.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Age</TableHead>
              <TableHead>Gender</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {patients.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{p.id}</TableCell>
                <TableCell>{p.name}</TableCell>
                <TableCell>{p.age}</TableCell>
                <TableCell>{p.gender}</TableCell>
                <TableCell>{p.phone}</TableCell>
                <TableCell>
                  <Link to={`/patients/${p.id}`} className="text-sm text-primary hover:underline">
                    View
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
```

- [ ] **Step 3: Implement PatientDetail**

Replace `src/features/patients/PatientDetail.tsx`:
```tsx
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getPatient } from '@/api/patients';
import { ErrorMessage } from '@/components/ErrorMessage';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export function PatientDetail() {
  const { id } = useParams<{ id: string }>();
  const { data: patient, isLoading, error } = useQuery({
    queryKey: ['patients', Number(id)],
    queryFn: () => getPatient(Number(id)),
    enabled: !!id,
  });

  if (isLoading) return <p className="text-muted-foreground">Loading...</p>;
  if (error) return <ErrorMessage message={(error as Error).message} />;
  if (!patient) return null;

  return (
    <div>
      <Link to="/patients">
        <Button variant="outline" className="mb-4">← Back to Patients</Button>
      </Link>
      <Card>
        <CardHeader>
          <CardTitle>{patient.name}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <p><span className="font-medium">ID:</span> {patient.id}</p>
          <p><span className="font-medium">Age:</span> {patient.age}</p>
          <p><span className="font-medium">Gender:</span> {patient.gender}</p>
          <p><span className="font-medium">Phone:</span> {patient.phone}</p>
        </CardContent>
      </Card>
    </div>
  );
}
```

- [ ] **Step 4: Verify in browser**

- Go to `http://localhost:5173/patients` → "No patients registered yet."
- Click "Register Patient" → dialog opens
- Submit with empty fields → validation errors appear under each field
- Fill all fields → click "Register Patient" → dialog closes, patient appears in table
- Click "View" → navigates to `/patients/1` showing patient card with all fields
- Click "← Back to Patients" → returns to list

- [ ] **Step 5: Commit**

```bash
git add src/features/patients/
git commit -m "feat(patients): add PatientList, PatientForm and PatientDetail"
```

---

## Task 7: Doctor feature

**Files:**
- Create: `src/features/doctors/DoctorForm.tsx`
- Create: `src/features/doctors/DoctorSchedule.tsx`
- Modify: `src/features/doctors/DoctorList.tsx`
- Modify: `src/features/doctors/DoctorDetail.tsx`

**Interfaces:**
- Consumes: `getAllDoctors`, `getDoctor`, `getDoctorSchedule`, `registerDoctor` from `src/api/doctors.ts`; `doctorRequestSchema`, `DoctorRequest`, `SPECIALIZATIONS` from `src/lib/schemas.ts`; `AppointmentStatus` from `src/types/index.ts`; shadcn `Select*`, `Badge`
- Produces: `/doctors` — table + dialog; `/doctors/:id` — doctor card + appointment schedule table

- [ ] **Step 1: Create DoctorForm**

Create `src/features/doctors/DoctorForm.tsx`:
```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { doctorRequestSchema, type DoctorRequest, SPECIALIZATIONS } from '@/lib/schemas';
import { registerDoctor } from '@/api/doctors';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ErrorMessage } from '@/components/ErrorMessage';
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select';

interface DoctorFormProps {
  onSuccess: () => void;
}

export function DoctorForm({ onSuccess }: DoctorFormProps) {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<DoctorRequest>({ resolver: zodResolver(doctorRequestSchema) });

  const { mutate, isPending, error } = useMutation({
    mutationFn: registerDoctor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
      onSuccess();
    },
  });

  return (
    <form onSubmit={handleSubmit((data) => mutate(data))} className="space-y-4">
      {error && <ErrorMessage message={(error as Error).message} />}
      <div className="space-y-1">
        <Label htmlFor="name">Name</Label>
        <Input id="name" {...register('name')} />
        {errors.name && <p className="text-sm text-red-600">{errors.name.message}</p>}
      </div>
      <div className="space-y-1">
        <Label>Specialization</Label>
        <Select onValueChange={(val) => setValue('specialization', val as DoctorRequest['specialization'])}>
          <SelectTrigger>
            <SelectValue placeholder="Select specialization" />
          </SelectTrigger>
          <SelectContent>
            {SPECIALIZATIONS.map((s) => (
              <SelectItem key={s} value={s}>
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {errors.specialization && (
          <p className="text-sm text-red-600">{errors.specialization.message}</p>
        )}
      </div>
      <div className="space-y-1">
        <Label htmlFor="phone">Phone</Label>
        <Input id="phone" {...register('phone')} />
        {errors.phone && <p className="text-sm text-red-600">{errors.phone.message}</p>}
      </div>
      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? 'Registering...' : 'Register Doctor'}
      </Button>
    </form>
  );
}
```

- [ ] **Step 2: Create DoctorSchedule**

Create `src/features/doctors/DoctorSchedule.tsx`:
```tsx
import { useQuery } from '@tanstack/react-query';
import { getDoctorSchedule } from '@/api/doctors';
import { ErrorMessage } from '@/components/ErrorMessage';
import { Badge } from '@/components/ui/badge';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table';
import type { AppointmentStatus } from '@/types';

const STATUS_VARIANT: Record<AppointmentStatus, 'default' | 'secondary' | 'destructive'> = {
  SCHEDULED: 'default',
  COMPLETED: 'secondary',
  CANCELLED: 'destructive',
};

interface DoctorScheduleProps {
  doctorId: number;
}

export function DoctorSchedule({ doctorId }: DoctorScheduleProps) {
  const { data: appointments, isLoading, error } = useQuery({
    queryKey: ['doctors', doctorId, 'schedule'],
    queryFn: () => getDoctorSchedule(doctorId),
  });

  if (isLoading) return <p className="text-muted-foreground">Loading schedule...</p>;
  if (error) return <ErrorMessage message={(error as Error).message} />;
  if (!appointments || appointments.length === 0)
    return <p className="text-muted-foreground">No upcoming appointments.</p>;

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>ID</TableHead>
          <TableHead>Patient</TableHead>
          <TableHead>Slot</TableHead>
          <TableHead>Status</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {appointments.map((appt) => (
          <TableRow key={appt.id}>
            <TableCell>{appt.id}</TableCell>
            <TableCell>{appt.patientName}</TableCell>
            <TableCell>{new Date(appt.slot).toLocaleString()}</TableCell>
            <TableCell>
              <Badge variant={STATUS_VARIANT[appt.status]}>{appt.status}</Badge>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
```

- [ ] **Step 3: Implement DoctorList**

Replace `src/features/doctors/DoctorList.tsx`:
```tsx
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getAllDoctors } from '@/api/doctors';
import { DoctorForm } from './DoctorForm';
import { Button } from '@/components/ui/button';
import { ErrorMessage } from '@/components/ErrorMessage';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table';

export function DoctorList() {
  const [open, setOpen] = useState(false);
  const { data: doctors, isLoading, error } = useQuery({
    queryKey: ['doctors'],
    queryFn: getAllDoctors,
  });

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Doctors</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>Register Doctor</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Register New Doctor</DialogTitle>
            </DialogHeader>
            <DoctorForm onSuccess={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      {isLoading && <p className="text-muted-foreground">Loading...</p>}
      {error && <ErrorMessage message={(error as Error).message} />}
      {doctors?.length === 0 && (
        <p className="text-muted-foreground">No doctors registered yet.</p>
      )}
      {doctors && doctors.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Name</TableHead>
              <TableHead>Specialization</TableHead>
              <TableHead>Phone</TableHead>
              <TableHead />
            </TableRow>
          </TableHeader>
          <TableBody>
            {doctors.map((d) => (
              <TableRow key={d.id}>
                <TableCell>{d.id}</TableCell>
                <TableCell>{d.name}</TableCell>
                <TableCell>{d.specialization}</TableCell>
                <TableCell>{d.phone}</TableCell>
                <TableCell>
                  <Link to={`/doctors/${d.id}`} className="text-sm text-primary hover:underline">
                    View
                  </Link>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
```

- [ ] **Step 4: Implement DoctorDetail**

Replace `src/features/doctors/DoctorDetail.tsx`:
```tsx
import { useParams, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { getDoctor } from '@/api/doctors';
import { DoctorSchedule } from './DoctorSchedule';
import { ErrorMessage } from '@/components/ErrorMessage';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export function DoctorDetail() {
  const { id } = useParams<{ id: string }>();
  const { data: doctor, isLoading, error } = useQuery({
    queryKey: ['doctors', Number(id)],
    queryFn: () => getDoctor(Number(id)),
    enabled: !!id,
  });

  if (isLoading) return <p className="text-muted-foreground">Loading...</p>;
  if (error) return <ErrorMessage message={(error as Error).message} />;
  if (!doctor) return null;

  return (
    <div>
      <Link to="/doctors">
        <Button variant="outline" className="mb-4">← Back to Doctors</Button>
      </Link>
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>{doctor.name}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <p><span className="font-medium">ID:</span> {doctor.id}</p>
          <p><span className="font-medium">Specialization:</span> {doctor.specialization}</p>
          <p><span className="font-medium">Phone:</span> {doctor.phone}</p>
        </CardContent>
      </Card>
      <h2 className="text-lg font-semibold mb-3">Schedule</h2>
      <DoctorSchedule doctorId={doctor.id} />
    </div>
  );
}
```

- [ ] **Step 5: Verify in browser**

- Go to `http://localhost:5173/doctors`
- Register a doctor with specialization from the dropdown → appears in table
- Click "View" → doctor card shows + "No upcoming appointments."
- Navigate to Appointments (next task), book one for this doctor, come back — schedule populates

- [ ] **Step 6: Commit**

```bash
git add src/features/doctors/
git commit -m "feat(doctors): add DoctorList, DoctorForm, DoctorDetail and DoctorSchedule"
```

---

## Task 8: Appointment feature

**Files:**
- Create: `src/features/appointments/AppointmentForm.tsx`
- Modify: `src/features/appointments/AppointmentList.tsx`

**Interfaces:**
- Consumes: `getAllAppointments`, `bookAppointment`, `cancelAppointment`, `completeAppointment` from `src/api/appointments.ts`; `appointmentRequestSchema`, `AppointmentRequest` from `src/lib/schemas.ts`; `AppointmentStatus` from `src/types/index.ts`; shadcn `Badge`, `Dialog*`, `Table*`, `Button`
- Produces: `/appointments` — table of all appointments, each row with Complete/Cancel buttons when status is SCHEDULED; "Book Appointment" dialog at top

- [ ] **Step 1: Create AppointmentForm**

Create `src/features/appointments/AppointmentForm.tsx`:
```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { appointmentRequestSchema, type AppointmentRequest } from '@/lib/schemas';
import { bookAppointment } from '@/api/appointments';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ErrorMessage } from '@/components/ErrorMessage';

interface AppointmentFormProps {
  onSuccess: () => void;
}

export function AppointmentForm({ onSuccess }: AppointmentFormProps) {
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<AppointmentRequest>({ resolver: zodResolver(appointmentRequestSchema) });

  const { mutate, isPending, error } = useMutation({
    mutationFn: bookAppointment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      onSuccess();
    },
  });

  return (
    <form onSubmit={handleSubmit((data) => mutate(data))} className="space-y-4">
      {error && <ErrorMessage message={(error as Error).message} />}
      <div className="space-y-1">
        <Label htmlFor="patientId">Patient ID</Label>
        <Input id="patientId" type="number" {...register('patientId')} />
        {errors.patientId && <p className="text-sm text-red-600">{errors.patientId.message}</p>}
      </div>
      <div className="space-y-1">
        <Label htmlFor="doctorId">Doctor ID</Label>
        <Input id="doctorId" type="number" {...register('doctorId')} />
        {errors.doctorId && <p className="text-sm text-red-600">{errors.doctorId.message}</p>}
      </div>
      <div className="space-y-1">
        <Label htmlFor="slot">Slot (Date & Time)</Label>
        <Input id="slot" type="datetime-local" {...register('slot')} />
        {errors.slot && <p className="text-sm text-red-600">{errors.slot.message}</p>}
      </div>
      <Button type="submit" disabled={isPending} className="w-full">
        {isPending ? 'Booking...' : 'Book Appointment'}
      </Button>
    </form>
  );
}
```

- [ ] **Step 2: Implement AppointmentList**

Replace `src/features/appointments/AppointmentList.tsx`:
```tsx
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllAppointments, cancelAppointment, completeAppointment } from '@/api/appointments';
import { AppointmentForm } from './AppointmentForm';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ErrorMessage } from '@/components/ErrorMessage';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from '@/components/ui/table';
import type { AppointmentStatus } from '@/types';

const STATUS_VARIANT: Record<AppointmentStatus, 'default' | 'secondary' | 'destructive'> = {
  SCHEDULED: 'default',
  COMPLETED: 'secondary',
  CANCELLED: 'destructive',
};

export function AppointmentList() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();

  const { data: appointments, isLoading, error } = useQuery({
    queryKey: ['appointments'],
    queryFn: getAllAppointments,
  });

  const { mutate: cancel } = useMutation({
    mutationFn: cancelAppointment,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });

  const { mutate: complete } = useMutation({
    mutationFn: completeAppointment,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['appointments'] }),
  });

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Appointments</h1>
        <Dialog open={open} onOpenChange={setOpen}>
          <DialogTrigger asChild>
            <Button>Book Appointment</Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Book New Appointment</DialogTitle>
            </DialogHeader>
            <AppointmentForm onSuccess={() => setOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      {isLoading && <p className="text-muted-foreground">Loading...</p>}
      {error && <ErrorMessage message={(error as Error).message} />}
      {appointments?.length === 0 && (
        <p className="text-muted-foreground">No appointments yet.</p>
      )}
      {appointments && appointments.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Patient</TableHead>
              <TableHead>Doctor</TableHead>
              <TableHead>Slot</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {appointments.map((appt) => (
              <TableRow key={appt.id}>
                <TableCell>{appt.id}</TableCell>
                <TableCell>{appt.patientName}</TableCell>
                <TableCell>{appt.doctorName}</TableCell>
                <TableCell>{new Date(appt.slot).toLocaleString()}</TableCell>
                <TableCell>
                  <Badge variant={STATUS_VARIANT[appt.status]}>{appt.status}</Badge>
                </TableCell>
                <TableCell className="space-x-2">
                  {appt.status === 'SCHEDULED' && (
                    <>
                      <Button size="sm" variant="outline" onClick={() => complete(appt.id)}>
                        Complete
                      </Button>
                      <Button size="sm" variant="destructive" onClick={() => cancel(appt.id)}>
                        Cancel
                      </Button>
                    </>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
```

- [ ] **Step 3: Verify in browser**

- Go to `http://localhost:5173/appointments`
- Click "Book Appointment" → fill in a valid Patient ID, Doctor ID, and a future slot → submit → row appears with SCHEDULED badge + Complete/Cancel buttons
- Click "Complete" → badge changes to COMPLETED, buttons disappear
- Book another, click "Cancel" → badge changes to CANCELLED
- Try booking the same doctor + same slot twice → error message shows from backend
- Try a past slot → Zod fires "Slot must be a future date and time" before submission

- [ ] **Step 4: Commit**

```bash
git add src/features/appointments/
git commit -m "feat(appointments): add AppointmentList and AppointmentForm with cancel and complete actions"
```
