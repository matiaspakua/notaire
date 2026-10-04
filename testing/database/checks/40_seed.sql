-- Rows inserted by the seed migrations (V2 initial data, V10 workflow demo data).
WITH expected(tbl, min_rows) AS (VALUES
    ('users', 1), ('people', 1), ('identification_types', 5), ('document_types', 4),
    ('folio_types', 3), ('management_statuses', 13), ('concepts', 4), ('procedure_types', 5),
    ('workflow_definition', 1), ('workflow_node', 9), ('workflow_transition', 8)
), counted AS (
    SELECT tbl, min_rows,
           (xpath('/row/c/text()',
                  query_to_xml(format('SELECT count(*) AS c FROM public.%I', tbl), false, true, '')))[1]::text::int AS actual
    FROM expected
)
SELECT CASE WHEN actual >= min_rows THEN 'PASS' ELSE 'FAIL' END
       || ' seed ' || tbl || ' has at least ' || min_rows || ' rows (found ' || actual || ')'
FROM counted ORDER BY tbl;

SELECT CASE WHEN count(*) = 1 THEN 'PASS' ELSE 'FAIL' END
       || ' default administrator user exists and is enabled'
FROM users WHERE username = 'admin' AND status;
