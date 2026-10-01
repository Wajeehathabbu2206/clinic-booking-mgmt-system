# MediBook

MediBook is a multi-clinic appointment booking and patient records platform built for small clinics that currently rely on manual or phone-based registration. It lets clinics manage staff, doctors, patients, appointments, and medical records in one system.

## Tech Stack

**Backend**
- Java 17+, Spring Boot 4.1.1 (modular monolith — separate packages per module, single build)
- Maven
- MySQL
- Spring Security + JWT-based authentication
- BCrypt password hashing

**Frontend**
- React + Vite
- Javascript
- Tailwind CSS

## Architecture

MediBook follows a modular monolith structure — each domain lives in its own package under a single deployable Spring Boot application.

**Backend modules**
```
common | auth | clinic | user | doctor | patient
appointment | slot | calendar | medicalrecord
notification | review | analytics
```

**Frontend feature folders**

Separate modules for admin, doctors, clinics, and patients
```
auth | clinic | doctor | patient | appointment
calendar | medical-record | notification | review
```

## Build Roadmap

The project is built in phases, backend before frontend. Feature implementation and test coverage are tracked separately below.

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Project setup (skeleton, config, health check) | ✅ Complete |
| 1 | Auth & Roles (JWT, register/login, role-based access) | ✅ Complete |
| 2 | Clinic & User Profiles (clinic CRUD, staff assignment) | ✅ Complete |
| 3 | Doctor Search |  ✅ Complete |
| 4 | Slot Management & Locking |  ✅ Completed |
| 5 | Appointment Booking | ✅ Completed |
| 6 | Calendar Views | ✅ Completed |
| 7 | Notifications | ✅ Completed |
| 8 | Medical Records | ✅ Completed |
| 9 | Reviews & Ratings | ✅ Completed |
| 10 | Analytics Dashboard | ✅ Completed |
| 11 | Polish & Testing | 🔄 In progress |

## Getting Started

### Prerequisites
- JDK 17+
- Maven
- MySQL
- Node.js + npm (for the frontend, once added)

### Backend Setup

1. Clone the repository
   ```bash
   git clone https://github.com/Wajeehathabbu2206/clinic-booking-mgmt-system.git
   cd clinic-booking-mgmt-system
   ```

2. Create a MySQL database for the project and update the connection details in `application-dev.yml` / `application-prod.yml` (or via environment variables).

3. Set the required environment variables before running the app. The initial SUPER_ADMIN account is seeded on startup from these values, so they must **never** be hardcoded in the committed config files:

   | Variable | Description |
   |----------|--------------|
   | `ADMIN_EMAIL` | Email for the seeded SUPER_ADMIN account |
   | `ADMIN_PASSWORD` | Password for the seeded SUPER_ADMIN account |
   | `ADMIN_FULL_NAME` | Display name for the seeded SUPER_ADMIN account |
   | `DB_URL` | MySQL JDBC connection URL |
   | `DB_USERNAME` | MySQL username |
   | `DB_PASSWORD` | MySQL password |
   | `JWT_SECRET` | JWT signing secret (at least 32 bytes for HS256) |
   | `FRONTEND_ORIGIN` | Allowed browser origin (defaults to `http://localhost:5173`) |
   | `EMAIL_PROVIDER` | `brevo` (default) or `console` for local development |
   | `BREVO_API_KEY` | Required when using the Brevo email provider |
   | `EMAIL_SENDER_ADDRESS` | Verified sender address required by Brevo |
   | `EMAIL_SENDER_NAME` | Optional display name for outgoing email |

   Example (`.env`, not committed):
   ```
   ADMIN_EMAIL=admin@medibook.com
   ADMIN_PASSWORD=randompassword
   ADMIN_FULL_NAME=Admin Name
   JWT_SECRET=replace-with-a-random-secret-of-at-least-32-bytes
   DB_URL=jdbc:mysql://localhost:3306/medibook
   DB_USERNAME=your_username
   DB_PASSWORD=your_db_password
   EMAIL_PROVIDER=console
   FRONTEND_ORIGIN=http://localhost:5173
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. Verify it's up via the health check endpoint:
   ```
   GET http://localhost:8080/api/health
   ```

### User Roles & Access

- New users registering via `/api/auth/register` default to the `PATIENT` role.
- A `SUPER_ADMIN` promotes a user to `CLINIC_ADMIN` via `PATCH /api/users/{id}/role`.
- Authorization uses the user's current database role, so role changes take effect on the next authenticated request.

### API Testing

No Postman collection is currently checked in. Automated coverage is limited; add API-level and database-backed integration tests before treating the backend as production-ready.

## Project Status

Backend feature phases 0–10 are implemented. Phase 11 is in progress; remaining hardening work includes versioned database migrations, broader API/integration test coverage, and maintained API request documentation.
