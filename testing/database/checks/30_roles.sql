-- Least-privilege metrics role created by migration V12. Run with: psql -v exporter=<name>
SELECT CASE WHEN count(*) = 1 THEN 'PASS' ELSE 'FAIL' END
       || ' exporter role exists and can log in'
FROM pg_roles WHERE rolname = :'exporter' AND rolcanlogin;

SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' exporter role is not a superuser'
FROM pg_roles WHERE rolname = :'exporter' AND (rolsuper OR rolcreatedb OR rolcreaterole);

SELECT CASE WHEN COALESCE(
         (SELECT array_agg(r.rolname ORDER BY r.rolname)
            FROM pg_auth_members m
            JOIN pg_roles r ON r.oid = m.roleid
            JOIN pg_roles u ON u.oid = m.member
           WHERE u.rolname = :'exporter'), '{}') = ARRAY['pg_monitor']::name[]
       THEN 'PASS' ELSE 'FAIL' END
       || ' exporter role is a member of pg_monitor only';

-- V51 (#1746): least-privilege SELECT for business extend-query gauges.
SELECT CASE WHEN count(*) = 4 THEN 'PASS' ELSE 'FAIL' END
       || ' exporter has SELECT on deeds, budgets, people, users'
FROM information_schema.role_table_grants
WHERE grantee = :'exporter'
  AND privilege_type = 'SELECT'
  AND table_schema = 'public'
  AND table_name IN ('deeds', 'budgets', 'people', 'users');
