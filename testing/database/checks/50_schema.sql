-- Structure of the migrated schema.
SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' every public table has a primary key'
FROM pg_tables t
WHERE t.schemaname = 'public'
  AND NOT EXISTS (SELECT 1 FROM pg_constraint c
                  WHERE c.conrelid = format('public.%I', t.tablename)::regclass AND c.contype = 'p');

SELECT CASE WHEN count(*) > 0 THEN 'PASS' ELSE 'FAIL' END
       || ' foreign keys exist (found ' || count(*) || ')'
FROM pg_constraint c JOIN pg_namespace n ON n.oid = c.connamespace
WHERE n.nspname = 'public' AND c.contype = 'f';

SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' every foreign key is validated'
FROM pg_constraint c JOIN pg_namespace n ON n.oid = c.connamespace
WHERE n.nspname = 'public' AND c.contype = 'f' AND NOT c.convalidated;
