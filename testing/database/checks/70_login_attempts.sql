\set QUIET on
-- Shared login lockout state (migration V48, issue #1634, SR-01).
SELECT CASE WHEN to_regclass('public.login_attempts') IS NOT NULL THEN 'PASS' ELSE 'FAIL' END
       || ' login_attempts table exists';

SELECT CASE WHEN COALESCE((SELECT array_agg(column_name::text ORDER BY column_name)
                             FROM information_schema.columns
                            WHERE table_schema = 'public' AND table_name = 'login_attempts'), '{}')
                  = ARRAY['failed_count', 'locked_until', 'updated_at', 'username']
       THEN 'PASS' ELSE 'FAIL' END
       || ' login_attempts holds only username, counter and timestamps';

SELECT CASE WHEN COALESCE((SELECT array_agg(a.attname::text)
                             FROM pg_index i
                             JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY (i.indkey)
                            WHERE i.indrelid = to_regclass('public.login_attempts') AND i.indisprimary), '{}')
                  = ARRAY['username']
       THEN 'PASS' ELSE 'FAIL' END
       || ' login_attempts is keyed by username';
