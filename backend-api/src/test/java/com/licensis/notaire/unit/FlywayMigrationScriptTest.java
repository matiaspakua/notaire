package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for Flyway migration scripts.
 * Verifies that SQL migration files follow Flyway naming conventions
 * and contain valid SQL syntax patterns.
 */
class FlywayMigrationScriptTest {

    private static final String MIGRATION_PATH = "src/main/resources/db/migration";

    @Test
    @DisplayName("Migration directory should exist")
    void migrationDirectoryShouldExist() {
        Path migrationDir = Paths.get(MIGRATION_PATH);
        assertThat(Files.exists(migrationDir))
                .as("Migration directory should exist at %s", MIGRATION_PATH)
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "V1__initial_schema.sql",
            "V2__initial_data.sql",
            "V52__migrate_seed_admin_password_to_bcrypt.sql"
    })
    @DisplayName("Migration files should follow Flyway naming convention")
    void migrationFilesShouldFollowNamingConvention(String filename) {
        Path migrationFile = Paths.get(MIGRATION_PATH, filename);
        assertThat(Files.exists(migrationFile))
                .as("Migration file %s should exist", filename)
                .isTrue();

        // Verify naming convention: V{version}__{description}.sql
        assertThat(filename).matches("^V\\d+__.+\\.sql$");
    }

    @Test
    @DisplayName("Migration files should not be empty")
    void migrationFilesShouldNotBeEmpty() throws IOException {
        Path schemaMigration = Paths.get(MIGRATION_PATH, "V1__initial_schema.sql");
        Path dataMigration = Paths.get(MIGRATION_PATH, "V2__initial_data.sql");

        long schemaSize = Files.size(schemaMigration);
        long dataSize = Files.size(dataMigration);

        assertThat(schemaSize)
                .as("Schema migration should not be empty")
                .isGreaterThan(100);

        assertThat(dataSize)
                .as("Data migration should not be empty")
                .isGreaterThan(50);
    }

    @Test
    @DisplayName("Schema migration should create all required tables")
    void schemaMigrationShouldCreateAllRequiredTables() throws IOException {
        String schemaContent = Files.readString(Paths.get(MIGRATION_PATH, "V1__initial_schema.sql"));

        String[] requiredTables = {
                "conceptos",
                "estados_de_gestion",
                "tipos_de_documento",
                "tipos_de_folio",
                "tipos_de_tramite",
                "tipos_identificacion",
                "personas",
                "inmuebles",
                "escrituras",
                "gestiones_de_escrituras",
                "presupuestos",
                "tramites",
                "usuarios",
                "registro_auditoria"
        };

        for (String tableName : requiredTables) {
            assertThat(schemaContent)
                    .as("Schema should contain CREATE TABLE for %s", tableName)
                    .contains("CREATE TABLE " + tableName);
        }
    }

    @Test
    @DisplayName("Data migration should insert reference data")
    void dataMigrationShouldInsertReferenceData() throws IOException {
        String dataContent = Files.readString(Paths.get(MIGRATION_PATH, "V2__initial_data.sql"));

        // Check for reference data inserts
        assertThat(dataContent).contains("INSERT INTO conceptos");
        assertThat(dataContent).contains("INSERT INTO estados_de_gestion");
        assertThat(dataContent).contains("INSERT INTO tipos_identificacion");
        assertThat(dataContent).contains("INSERT INTO personas");
        assertThat(dataContent).contains("INSERT INTO usuarios");
    }

    @Test
    @DisplayName("V2 historical seed still records MD5(admin); V52 migrates it to BCrypt")
    void adminUserShouldHaveHashedPassword() throws IOException {
        String dataContent = Files.readString(Paths.get(MIGRATION_PATH, "V2__initial_data.sql"));

        // MD5 hash of "admin" is 21232f297a57a5a743894a0e4a801fc3 (historical V2 text)
        assertThat(dataContent)
                .as("Admin password should be MD5 hashed in V2 (historical)")
                .contains("21232f297a57a5a743894a0e4a801fc3");

        // Should have admin user insert
        assertThat(dataContent)
                .as("Should insert admin user")
                .contains("INSERT INTO usuarios");

        // Should reference the notary person
        assertThat(dataContent)
                .as("Admin user should reference notary")
                .contains("Escribano");

        String v52 = Files.readString(Paths.get(MIGRATION_PATH, "V52__migrate_seed_admin_password_to_bcrypt.sql"));
        assertThat(v52)
                .as("V52 replaces the V2 MD5 seed with BCrypt (#1762)")
                .contains("21232f297a57a5a743894a0e4a801fc3")
                .contains("$2a$12$MzGVbtKng5BRII/pfWCJpOE.hkzzYSLroZOjzATlTdRyygKG8Z0e6");
    }

    @Test
    @DisplayName("All SQL statements should end with semicolon")
    void sqlStatementsShouldEndWithSemicolon() throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(MIGRATION_PATH, "V1__initial_schema.sql"))) {
            long linesWithSemicolon = lines
                    .filter(line -> !line.trim().isEmpty())
                    .filter(line -> !line.trim().startsWith("--"))
                    .filter(line -> line.trim().endsWith(";"))
                    .count();

            assertThat(linesWithSemicolon)
                    .as("Most SQL statements should end with semicolon")
                    .isGreaterThan(20);
        }
    }

    @Test
    @DisplayName("Schema migration should define primary keys")
    void schemaMigrationShouldDefinePrimaryKeys() throws IOException {
        String schemaContent = Files.readString(Paths.get(MIGRATION_PATH, "V1__initial_schema.sql"));

        // Primary key declarations
        assertThat(schemaContent).contains("PRIMARY KEY");
        assertThat(schemaContent).contains("SERIAL PRIMARY KEY");
    }

    @Test
    @DisplayName("Schema migration should define foreign keys")
    void schemaMigrationShouldDefineForeignKeys() throws IOException {
        String schemaContent = Files.readString(Paths.get(MIGRATION_PATH, "V1__initial_schema.sql"));

        // Foreign key declarations
        assertThat(schemaContent).contains("FOREIGN KEY");
        assertThat(schemaContent).contains("REFERENCES");
    }

    @Test
    @DisplayName("Should have at least 2 migration files")
    void shouldHaveAtLeastTwoMigrationFiles() throws IOException {
        try (Stream<Path> migrationFiles = Files.list(Paths.get(MIGRATION_PATH))) {
            long count = migrationFiles
                    .filter(path -> path.toString().endsWith(".sql"))
                    .count();

            assertThat(count)
                    .as("Should have at least 2 migration files")
                    .isGreaterThanOrEqualTo(2);
        }
    }
}
