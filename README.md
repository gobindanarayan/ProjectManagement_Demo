# Manik Agro Internal Project Management System

The Manik Agro Project Management System helps staff manage projects in one place. It includes a login page and dashboard to track progress, budgets, and recent activity. Users can view project details, coordinate team members, manage tasks, and record expenses. The AJAH record uses the neutral description in the project brief; its numbers and linked sample records are fictional and should be verified or replaced by authorized company personnel.

## Features

- Responsive dashboard with project counts, budget totals, expenditure, remaining budget, active project members, charts, activity, and upcoming deadlines.
- Project search and status filters, project details, project creation and editing, and protected project deletion.
- Project team assignment, task management, and expense entry, editing, and deletion.
- Employee account management with ADMIN and EMPLOYEE roles.
- REST APIs with server-side validation, consistent JSON errors, session authentication, BCrypt password hashing, CSRF protection, and role checks.
- Database-backed demo records. The application initializes the sample projects only when the project table is empty.

## Technology

- Java 25, Spring Boot 3.5.16, Spring MVC, Spring Security, Spring Data JPA, Hibernate, Jakarta Bean Validation
- Maven
- MySQL 8.0+
- HTML, CSS, and JavaScript; the static frontend is served by Spring Boot
- H2 for isolated API integration tests

Spring Boot 3.5 supports Java 25 according to the [official system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html).

## Prerequisites

- JDK 25 (a JRE alone is not sufficient)
- MySQL 8.0 or later, running locally or at a reachable internal database host
- A modern browser

The included Maven Wrapper downloads and uses Maven 3.9.16 on its first run, so Maven does not need to be installed separately. The first wrapper/build run needs access to the Maven distribution and dependencies.

## MySQL setup

Create a database and an application account with a strong local secret. The example below is for a local development machine; replace the password before running it.

```sql
CREATE DATABASE manik_pms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'manik_pms_app'@'localhost' IDENTIFIED BY 'replace-with-a-local-secret';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON manik_pms.* TO 'manik_pms_app'@'localhost';
```

Hibernate creates or updates the tables on startup when `DB_DDL_MODE=update`. This is convenient for local development. Before an internal deployment, use a reviewed schema migration and set `DB_DDL_MODE=validate`.

## Configuration

Copy `.env.example` as a reference. Spring Boot does not automatically read a `.env` file, so set the values in your shell or use an environment manager. Do not commit real credentials.

PowerShell example:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/manik_pms?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC'
$env:DB_USERNAME = 'manik_pms_app'
$env:DB_PASSWORD = 'your-local-database-secret'
$env:DB_DDL_MODE = 'update'
$env:PMS_SEED_DEMO_DATA = 'true'
```

Environment settings:

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_URL` | MySQL JDBC URL | Local `manik_pms` database |
| `DB_USERNAME` | MySQL account | `root` |
| `DB_PASSWORD` | MySQL password | Empty for local setup only |
| `DB_DDL_MODE` | Hibernate schema handling | `update` |
| `PMS_SEED_DEMO_DATA` | Insert three labeled demo projects when no projects exist | `true` |
| `PORT` | HTTP port | `8080` |

### First administrator account

The demo administrator is stored in MySQL; no PowerShell setup is needed for the account. Start the application once from your Java IDE so Hibernate creates the tables. Then open `database/demo-admin.sql` in MySQL Workbench, select the `manik_pms` database, and execute the script:

- **Username:** `demo-admin`
- **Password:** `ManikAgroDemo!2026`

The SQL script stores a BCrypt hash, not the plaintext password. It creates or resets only the local demo administrator; do not run it against a deployment database. Seeded sample employees have generated, unknown passwords; an administrator can provision their accounts through Employees.

## Run the application

To launch without a terminal, open this folder as a Maven project in your Java IDE, set the run JDK to Java 25, and run `ManikPmsApplication`. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the IDE's run configuration if your MySQL connection differs from the local defaults.

From a terminal, after configuring MySQL and the environment:

```powershell
.\mvnw.cmd spring-boot:run
```

Or build and run the executable JAR:

```powershell
.\mvnw.cmd clean package
java -jar target/internal-pms-1.0.0.jar
```

Open [http://localhost:8080](http://localhost:8080) and sign in with the demo administrator account created by the SQL script. The frontend and API share the same origin, so no separate frontend server or CORS configuration is needed.

## Run tests

The integration tests use an isolated in-memory H2 database and do not require a running MySQL server:

```powershell
.\mvnw.cmd test
```

The project API tests cover persisted expense totals, remaining-budget calculations, duplicate project codes, and employee role restrictions. Run them in the target deployment environment as part of its release process as well.

## Main API routes

All `/api/**` routes require a signed-in session except `/api/auth/login` and `/api/auth/csrf`.

| Method and route | Role | Purpose |
| --- | --- | --- |
| `GET /api/auth/csrf` | Public | Issue/read the CSRF token for the browser session |
| `POST /api/auth/login` | Public | Form-encoded username and password login |
| `GET /api/auth/me` | Signed in | Current employee account |
| `POST /api/auth/logout` | Signed in | End the session |
| `GET /api/dashboard` | Signed in | Database-calculated totals, projects, activity, deadlines |
| `GET /api/projects?search=&status=` | Signed in | Search/filter project cards |
| `POST /api/projects` | ADMIN | Create a project |
| `GET /api/projects/{id}` | Signed in | Project, financial summary, team, tasks and expenses |
| `PUT /api/projects/{id}` / `DELETE /api/projects/{id}` | ADMIN | Edit or delete a project (expense history prevents deletion) |
| `/api/projects/{id}/members` | ADMIN for changes | Assign a member or deactivate a project assignment |
| `/api/projects/{id}/tasks` | ADMIN for changes | Create, edit, and delete tasks |
| `/api/projects/{id}/expenses` | ADMIN for changes | List, create, edit, and delete expense records |
| `/api/employees` | ADMIN | List, create, update, and deactivate employee accounts |
| `GET /api/activity?page=0&size=20&projectId=` | Signed in | Paginated recent activity with total counts (page size capped at 100) |

Mutation requests use JSON, except the Spring Security login endpoint which accepts URL-encoded form fields. Browser mutation requests send the `X-XSRF-TOKEN` header from the `XSRF-TOKEN` cookie.

## Project structure

```text
src/main/java/com/manikagro/pms/
  config/       Spring Security and development administrator bootstrap
  controller/   REST endpoints
  domain/       JPA entities and enums
  dto/          Validated request/response records
  exception/    API error responses
  repository/   Spring Data JPA repositories
  service/      Business rules, calculations, initialization, DTO mapping
src/main/resources/
  application.properties
  static/       HTML, CSS and JavaScript application
src/test/       H2-backed API integration tests
database/       Database setup notes
docs/           Project documentation and employee guide
.mvn/wrapper/   Pinned Maven Wrapper configuration
mvnw, mvnw.cmd  Unix and Windows Maven Wrapper scripts
```

## Troubleshooting

- **Cannot connect to MySQL:** check that MySQL is running, the database exists, and `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` match the local account.
- **Login rejected:** for the local demo, run `database/demo-admin.sql` against the configured database, then sign in as `demo-admin` with `ManikAgroDemo!2026`. Otherwise ask an existing administrator to provision your employee account.
- **CSRF error:** reload the application so it can fetch a fresh CSRF cookie, then sign in again.
- **Cannot delete a project:** projects with recorded expenses retain their financial history. Set the project status to Cancelled instead.
- **Project has no manager choices:** an administrator must create or activate an employee first.
- **Demo data is present:** it is intentionally fictional. Set `PMS_SEED_DEMO_DATA=false` to disable initialization on future startup; existing records remain in the database and must be reviewed by authorized personnel.
- **Build reports a Java version error:** confirm `java -version` and Maven both use JDK 25.
- **The wrapper cannot start:** allow its first run to download Maven 3.9.16, and confirm the JDK is on `PATH`.

## Deployment note

This repository is an implementation baseline, not a claim of production readiness. Before internal rollout, configure TLS and secure infrastructure, review employee access and retention policies, use schema migrations and backups, change default infrastructure settings, and complete the documented deployment and security verification with company personnel.
