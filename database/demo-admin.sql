-- Local demonstration account only. Do not run against a deployment database.
-- Sign in with username: demo-admin
-- Demo password: ManikAgroDemo!2026
-- The application uses BCrypt; only the hash is stored in employees.password_hash.

USE manik_pms;

SET @demo_admin_password_hash = '$2a$12$e7l/dhpNcXBAAOx6b.fKZuPP3cShXbVnZ9sQwSey3ahRT7mRc0/J6';

INSERT IGNORE INTO employees
    (employee_code, name, username, email, password_hash, job_title, `role`, active, created_at)
VALUES
    ('ADMIN-DEMO-01', 'Demo Administrator', 'demo-admin', 'demo-admin@example.invalid',
     @demo_admin_password_hash, 'System Administrator', 'ADMIN', TRUE, CURRENT_TIMESTAMP);

UPDATE employees
SET password_hash = @demo_admin_password_hash,
    `role` = 'ADMIN',
    active = TRUE
WHERE username = 'demo-admin';

SELECT employee_code, name, username, `role`, active
FROM employees
WHERE username = 'demo-admin';
