# Database initialization

Create the MySQL database and application account using the example SQL in the repository README, then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in the application environment. Hibernate initializes and updates local development tables from the JPA entity mappings when `DB_DDL_MODE=update`.

The demo initializer runs only when `PMS_SEED_DEMO_DATA=true` and the `projects` table is empty. It stores all three starter projects, demo employees with random unusable password hashes, team assignments, tasks, expenses, and initial activity records. Every seeded project has its `demo_data` flag set; sample expense descriptions and the two demo project descriptions also say they are fictional. AJAH's description remains the neutral wording from the project brief.

After the application creates its tables, run `demo-admin.sql` in MySQL Workbench against the `manik_pms` database to add the local demo administrator. It stores a BCrypt hash in `employees.password_hash`, not the plaintext password. The username is `demo-admin` and the demo-only password is `ManikAgroDemo!2026`. Re-running the script resets that account's password. Never apply this script to a deployment database.

For an existing database, disabling `PMS_SEED_DEMO_DATA` stops future seeding but does not delete existing demo rows. Authorized personnel must review or replace those values deliberately. Before a managed deployment, create a reviewed migration baseline, use `DB_DDL_MODE=validate`, and maintain tested backups.
