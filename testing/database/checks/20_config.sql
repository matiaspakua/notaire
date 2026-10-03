-- Server and database configuration.
SELECT CASE WHEN current_setting('server_version_num')::int / 10000 = 16 THEN 'PASS' ELSE 'FAIL' END
       || ' server major version is 16 (found ' || current_setting('server_version_num')::int / 10000 || ')';

SELECT CASE WHEN pg_encoding_to_char(encoding) = 'UTF8' THEN 'PASS' ELSE 'FAIL' END
       || ' database encoding is UTF8 (found ' || pg_encoding_to_char(encoding) || ')'
FROM pg_database WHERE datname = current_database();
