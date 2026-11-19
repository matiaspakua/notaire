\set QUIET on
-- Shared API quota windows (migration V55, issue #2010, SR-15).
SELECT CASE WHEN to_regclass('public.api_rate_windows') IS NOT NULL THEN 'PASS' ELSE 'FAIL' END
       || ' api_rate_windows table exists';

SELECT CASE WHEN COALESCE((SELECT array_agg(column_name::text ORDER BY column_name)
                             FROM information_schema.columns
                            WHERE table_schema = 'public' AND table_name = 'api_rate_windows'), '{}')
                  = ARRAY['bucket_key', 'request_count', 'window_start']
       THEN 'PASS' ELSE 'FAIL' END
       || ' api_rate_windows holds only the bucket key, the count and the window start';

SELECT CASE WHEN COALESCE((SELECT array_agg(a.attname::text)
                             FROM pg_index i
                             JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY (i.indkey)
                            WHERE i.indrelid = to_regclass('public.api_rate_windows') AND i.indisprimary), '{}')
                  = ARRAY['bucket_key']
       THEN 'PASS' ELSE 'FAIL' END
       || ' api_rate_windows is keyed by bucket_key';
