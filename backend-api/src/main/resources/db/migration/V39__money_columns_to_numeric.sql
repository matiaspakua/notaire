-- =============================================================================
-- V39__money_columns_to_numeric.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-02
-- Description: Convert all monetary columns from real/unconstrained NUMERIC to
--              explicit NUMERIC scale (issue #1061 / CU15). Currency uses
--              NUMERIC(19,2); variable_percentage uses NUMERIC(7,4).
--              Layout floats (workflow node positions) are intentionally skipped.
-- =============================================================================

-- Currency columns previously stored as real
ALTER TABLE payments
    ALTER COLUMN amount TYPE NUMERIC(19, 2)
    USING ROUND(amount::numeric, 2);

ALTER TABLE concepts
    ALTER COLUMN amount TYPE NUMERIC(19, 2)
    USING ROUND(amount::numeric, 2);

ALTER TABLE items
    ALTER COLUMN amount TYPE NUMERIC(19, 2)
    USING ROUND(amount::numeric, 2);

ALTER TABLE budgets
    ALTER COLUMN property_amount TYPE NUMERIC(19, 2)
    USING ROUND(property_amount::numeric, 2);

ALTER TABLE properties
    ALTER COLUMN fiscal_valuation TYPE NUMERIC(19, 2)
    USING ROUND(fiscal_valuation::numeric, 2);

ALTER TABLE submitted_documents
    ALTER COLUMN amount_to_pay TYPE NUMERIC(19, 2)
    USING ROUND(amount_to_pay::numeric, 2);

ALTER TABLE document_types
    ALTER COLUMN amount_to_pay TYPE NUMERIC(19, 2)
    USING ROUND(amount_to_pay::numeric, 2);

-- Already NUMERIC (V22 / V24) — tighten precision/scale
ALTER TABLE document_cost_templates
    ALTER COLUMN fixed_amount TYPE NUMERIC(19, 2)
    USING ROUND(fixed_amount::numeric, 2);

ALTER TABLE document_cost_templates
    ALTER COLUMN variable_percentage TYPE NUMERIC(7, 4)
    USING ROUND(variable_percentage::numeric, 4);

ALTER TABLE registration_drafts
    ALTER COLUMN operation_price TYPE NUMERIC(19, 2)
    USING ROUND(operation_price::numeric, 2);
