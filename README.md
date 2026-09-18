# MediBook

MediBook is a multi-clinic appointment booking and patient records platform built for small clinics that currently rely on manual or phone-based registration. It lets clinics manage staff, doctors, patients, appointments, and medical records in one system.

## Tech Stack

**Backend**
- Java, Spring Boot (modular monolith — separate packages per module, single build)
- Maven
- MySQL
- Spring Security + JWT-based authentication
- BCrypt password hashing

**Frontend**
- React + Vite
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
```
auth | clinic | doctor | patient | appointment
calendar | medical-record | notification | review
```

## Build Roadmap

The project is built in phases, backend before frontend, with each phase completed and tested before moving to the next.

| Phase | Description | Status |
|-------|-------------|--------|
| 0 | Project setup (skeleton, config, health check) | ✅ Complete |
| 1 | Auth & Roles (JWT, register/login, role-based access) | ✅ Complete |
| 2 | Clinic & User Profiles (clinic CRUD, staff assignment) | ✅ Complete |
| 3 | Doctor Search |  ✅ Complete |
| 4 | Slot Management & Locking | 🔜 Planned |
| 5 | Appointment Booking | 🔜 Planned |
| 6 | Calendar Views | 🔜 Planned |
| 7 | Notifications | 🔜 Planned |
| 8 | Medical Records | 🔜 Planned |
| 9 | Reviews & Ratings | 🔜 Planned |
| 10 | Analytics Dashboard | 🔜 Planned |
| 11 | Polish & Testing | 🔜 Planned |

## Getting Started

### Prerequisites
- JDK 17+
- Maven
- MySQL
- Node.js + npm (for the frontend, once added)

### Backend Setup

1. Clone the repository
   ```bash
   git clone https://github.com/<your-username>/medibook.git
   cd medibook
   ```

2. Create a MySQL database for the project and update the connection details in `application-dev.yml` / `application-product.yml` (or via environment variables — see below).

3. Set the required environment variables before running the app. The initial SUPER_ADMIN account is seeded on startup from these values, so they must **never** be hardcoded in the committed config files:

   | Variable | Description |
   |----------|--------------|
   | `ADMIN_EMAIL` | Email for the seeded SUPER_ADMIN account |
   | `ADMIN_PASSWORD` | Password for the seeded SUPER_ADMIN account |
   | `ADMIN_FULL_NAME` | Display name for the seeded SUPER_ADMIN account |
   | `DB_URL` | MySQL JDBC connection URL |
   | `DB_USERNAME` | MySQL username |
   | `DB_PASSWORD` | MySQL password |

   Example (`.env`, not committed):
   ```
   ADMIN_EMAIL=admin@medibook.com
   ADMIN_PASSWORD=change_this_before_running
   ADMIN_FULL_NAME=System Administrator
   DB_URL=jdbc:mysql://localhost:3306/medibook
   DB_USERNAME=root
   DB_PASSWORD=your_db_password
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
- Promoted users must log in again to receive a JWT with updated role claims.

### API Testing

A full Postman collection (endpoint, method, headers, and sample request bodies) is maintained alongside each phase's delivery. See `/docs/postman` *(update this path once added to the repo)*.

## Project Status

Currently on **Phase 2 (Clinic & User Profiles)** — complete. Development continues phase by phase; see the roadmap above for what's next.
