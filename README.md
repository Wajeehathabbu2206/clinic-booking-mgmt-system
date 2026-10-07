# MediBook

MediBook is a multi-clinic appointment booking and patient records platform. It brings clinic, staff, doctor, patient, appointment, and medical record workflows together in one application.

This repository is a monorepo with a Spring Boot REST API and a React web client.

## Project structure

```text
clinic-booking-system/
├── clinic-booking-system-backend/   # Java / Spring Boot REST API
├── clinic-booking-system-frontend/  # React / TypeScript web app
└── README.md
```

The backend is a modular monolith: domain areas share one Spring Boot application and deployment.

| Area | Responsibilities |
| --- | --- |
| `auth`, `user` | Registration, login, profiles, and roles |
| `clinic`, `doctor`, `doctorapplication` | Clinic and staff management, doctor discovery, and doctor applications |
| `slot`, `appointment`, `calendar` | Availability, bookings, scheduling, and holidays |
| `medicalrecord`, `review`, `notification` | Patient records, ratings, and notifications |
| `analytics`, `common` | Dashboards, shared configuration, responses, and error handling |

The frontend is a React + TypeScript single-page app built with Vite, React Router, Axios, and Tailwind CSS. Its routes and shared UI/API foundations are in place; several user-facing flows are still placeholders as the frontend is being built out.

## Technology

- **Backend:** Java 17, Spring Boot 4, Maven, Spring Web, Spring Data JPA, Spring Security, JWT, MySQL
- **Frontend:** React 18, TypeScript, Vite, React Router, Axios, Tailwind CSS
- **Email:** Brevo provider by default; console provider is available for local development

## Prerequisites

- JDK 17 or later
- MySQL 8 or a compatible MySQL server
- Node.js and npm
- Git

The backend Maven wrapper is included, so a separate Maven installation is not required.

## Run locally

### 1. Configure the database and backend

Create a database, for example:

```sql
CREATE DATABASE medibook;
```

Create `clinic-booking-system-backend/.env` (this file is ignored by Git) with your local settings:

```dotenv
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=replace-with-a-strong-local-password
ADMIN_FULL_NAME=System Administrator
JWT_SECRET=replace-with-a-random-secret-at-least-32-bytes-long
DB_URL=jdbc:mysql://localhost:3306/medibook
DB_USERNAME=your_mysql_username
DB_PASSWORD=your_mysql_password
EMAIL_PROVIDER=console
FRONTEND_ORIGIN=http://localhost:5173
```

On startup, the application creates or updates tables through Hibernate and seeds the initial `SUPER_ADMIN` from the `ADMIN_*` settings. Keep real credentials and signing keys out of source control. To send email through Brevo, set `EMAIL_PROVIDER=brevo`, `BREVO_API_KEY`, and a verified `EMAIL_SENDER_ADDRESS`; `EMAIL_SENDER_NAME` is optional.

Run the backend from its project directory:

```bash
cd clinic-booking-system-backend
./mvnw spring-boot:run
```

On Windows PowerShell, use:

```powershell
cd clinic-booking-system-backend
.\mvnw.cmd spring-boot:run
```

The `dev` profile is active by default. The API listens on `http://localhost:8080`; check that it is running at [`/api/health`](http://localhost:8080/api/health).

### 2. Run the frontend

In a second terminal, from the repository root:

```bash
cd clinic-booking-system-frontend
npm install
npm run dev
```

Open the Vite URL printed in the terminal (normally `http://localhost:5173`). The checked-in development environment points the client at `http://localhost:8080/api`. Set `VITE_API_BASE_URL` in a local `.env.development` file if your API runs elsewhere.

## Useful commands

Run these from their respective project directories.

| Project | Command | Purpose |
| --- | --- | --- |
| Frontend | `npm run dev` | Start the Vite development server |
| Frontend | `npm run typecheck` | Check TypeScript types |
| Frontend | `npm run build` | Type-check and create a production build in `dist/` |
| Frontend | `npm run preview` | Preview the production build locally |
| Backend | `./mvnw test` | Run backend tests |
| Backend | `./mvnw package` | Build the backend JAR |

Use `mvnw.cmd` instead of `./mvnw` in Windows PowerShell.

## API overview

The REST API is rooted at `/api`. Main resource groups include:

- `/auth` — registration and login
- `/users`, `/clinics`, `/doctors`, `/doctor-applications` — accounts and clinic/doctor administration
- `/slots`, `/calendar`, `/appointments` — availability and scheduling
- `/medical-records`, `/reviews`, `/notifications` — patient and communication workflows
- `/dashboard` — role-specific dashboard data
- `/health` — service health check

Most endpoints require a bearer access token returned by login. Access is controlled by the authenticated user's role and resource permissions. The backend README contains additional backend setup and role notes.

## Configuration reference

| Variable | Purpose |
| --- | --- |
| `ADMIN_EMAIL` | Email for the startup-seeded `SUPER_ADMIN` |
| `ADMIN_PASSWORD` | Password for the startup-seeded `SUPER_ADMIN` |
| `ADMIN_FULL_NAME` | Display name for the startup-seeded account |
| `DB_URL` | MySQL JDBC URL, such as `jdbc:mysql://localhost:3306/medibook` |
| `DB_USERNAME`, `DB_PASSWORD` | MySQL credentials |
| `JWT_SECRET` | JWT signing secret (at least 32 bytes for HS256) |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds (default: 86400000) |
| `FRONTEND_ORIGIN` | Allowed browser origin (default: `http://localhost:5173`) |
| `EMAIL_PROVIDER` | `brevo` |
| `BREVO_API_KEY` | Brevo API key when using the Brevo provider |
| `EMAIL_SENDER_ADDRESS` | Verified sender address for Brevo |
| `EMAIL_SENDER_NAME` | Sender display name (default: `MediBook`) |
| `VITE_API_BASE_URL` | Frontend API base URL (default development value: `http://localhost:8080/api`) |

## Current status

Backend capabilities include authentication and role-based access, clinic and doctor management, slots and appointments, calendars, notifications, medical records, reviews, and dashboards. The frontend currently provides the application shell, shared UI components, API client, and route structure, with feature pages under active development. See the frontend and backend READMEs for component-specific details.

## Security and data

- Do not commit `.env` files, production credentials, JWT secrets, or real patient data.
- The local configuration uses Hibernate schema updates for development. Use a deliberate migration strategy and production-specific configuration before deploying to production.
- The project handles sensitive health information; production operation requires appropriate security, privacy, access-control, backup, and regulatory review.
