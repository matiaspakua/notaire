-- Flyway history after `migrate` on an empty database. Every line printed is PASS or FAIL.
SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' no failed migration in flyway_schema_history'
FROM flyway_schema_history WHERE NOT success;

SELECT CASE WHEN count(*) = max(version::int) THEN 'PASS' ELSE 'FAIL' END
       || ' versions are contiguous from 1 to ' || max(version::int)
FROM flyway_schema_history WHERE version IS NOT NULL;

SELECT CASE WHEN count(*) = 0 THEN 'PASS' ELSE 'FAIL' END
       || ' every versioned migration succeeded'
FROM flyway_schema_history WHERE version IS NOT NULL AND NOT success;
