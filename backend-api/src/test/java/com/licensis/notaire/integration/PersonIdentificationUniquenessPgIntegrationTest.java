package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * CU17 / CU18 — Flyway-managed PostgreSQL uniqueness for
 * {@code people (fk_id_tipo_identificacion, identification_number)} (Issue #799).
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@RequirementCoverage({"CU17", "CU18"})
@DisplayName("Person identification uniqueness against real Flyway schema (CU17/CU18)")
class PersonIdentificationUniquenessPgIntegrationTest extends BaseIntegrationTest {

    private static final String UNIQUE_INDEX = "uq_people_identification_type_number";

    private static final String DUPLICATE_PRECHECK_SQL = """
            DO $$
            DECLARE
              duplicate_group_count integer;
            BEGIN
              SELECT COUNT(*) INTO duplicate_group_count
              FROM (
                SELECT fk_id_tipo_identificacion, identification_number
                FROM people
                GROUP BY fk_id_tipo_identificacion, identification_number
                HAVING COUNT(*) > 1
              ) duplicate_groups;
              IF duplicate_group_count > 0 THEN
                RAISE EXCEPTION
                  'Cannot enforce people identification uniqueness: % duplicate '
                  '(fk_id_tipo_identificacion, identification_number) group(s) exist. '
                  'Resolve duplicates (prefer keeping the lowest id) before retrying.',
                  duplicate_group_count;
              END IF;
            END $$;
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should expose the unique index after Flyway migrates")
    void shouldExposeUniqueIndexAfterFlywayMigrates() {
        Integer indexCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM pg_indexes
                WHERE schemaname = 'public' AND indexname = ?
                """,
                Integer.class,
                UNIQUE_INDEX);

        assertThat(indexCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject a second JDBC insert with the same type and number")
    void shouldRejectSecondJdbcInsertWithSameTypeAndNumber() {
        String document = "799pg-" + UUID.randomUUID().toString().substring(0, 8);
        insertPerson("799-A", document, 1);
        assertThatThrownBy(() -> insertPerson("799-B", document, 1))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Should fail the migration pre-check when duplicate groups exist")
    void shouldFailPrecheckWhenDuplicateGroupsExist() {
        jdbcTemplate.execute("DROP INDEX IF EXISTS " + UNIQUE_INDEX);

        String document = "799dup-" + UUID.randomUUID().toString().substring(0, 8);
        insertPerson("Dup-A", document, 1);
        insertPerson("Dup-B", document, 1);

        assertThatThrownBy(() -> jdbcTemplate.execute(DUPLICATE_PRECHECK_SQL))
                .hasMessageContaining("duplicate");

        // Restore uniqueness for later tests in this class / container.
        jdbcTemplate.execute("""
                DELETE FROM people p
                USING people keeper
                WHERE p.fk_id_tipo_identificacion = keeper.fk_id_tipo_identificacion
                  AND p.identification_number = keeper.identification_number
                  AND p.id > keeper.id
                """);
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS " + UNIQUE_INDEX
                        + " ON people (fk_id_tipo_identificacion, identification_number)");
    }

    private void insertPerson(String lastName, String identificationNumber, int typeId) {
        jdbcTemplate.update(
                """
                INSERT INTO people (
                    version, first_name, last_name, identification_number, is_client,
                    fk_id_tipo_identificacion
                ) VALUES (0, 'Issue799', ?, ?, true, ?)
                """,
                lastName,
                identificationNumber,
                typeId);
    }
}
