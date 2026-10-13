\set QUIET on
-- A database always has an administrator, and the seeded admin account is not a notary
-- (migration V49, issue #1638, SR-16).
SELECT CASE WHEN count(*) >= 1 THEN 'PASS' ELSE 'FAIL' END
       || ' an enabled administrator account exists'
FROM users WHERE status AND lower(trim(user_type)) IN ('admin', 'administrador');

SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' the account named admin is not stored as a notary'
FROM users WHERE lower(username) = 'admin' AND lower(trim(user_type)) = 'escribano';
