package com.licensis.notaire.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Locks the English names of constraints that PostgreSQL kept when tables were renamed
 * (issue #1490, CU75). Written first: it fails until V46 renames them.
 */
@DisplayName("English constraint names (#1490)")
class EnglishConstraintNamesMigrationTest {

    private static final List<String> RENAMES = List.of(
            "audit_records|registro_auditoria_fk_id_usuario_fkey|audit_records_fk_id_user_fkey",
            "budgets|presupuestos_fk_id_persona_fkey|budgets_fk_id_person_fkey",
            "budgets|presupuestos_pkey|budgets_pkey",
            "budget_templates|plantilla_presupuestos_fk_id_concepto_fkey|budget_templates_fk_id_concept_fkey",
            "budget_templates|plantilla_presupuestos_fk_id_tipo_tramite_fkey|budget_templates_fk_id_procedure_type_fkey",
            "budget_templates|plantilla_presupuestos_pkey|budget_templates_pkey",
            "copies|copias_fk_id_persona_fkey|copies_fk_id_person_fkey",
            "copies|copias_fk_id_testimonio_fkey|copies_fk_id_testimony_fkey",
            "copies|copias_pkey|copies_pkey",
            "deed_managements|gestiones_de_escrituras_fk_id_estado_de_gestion_fkey|deed_managements_fk_id_management_status_fkey",
            "deed_managements|gestiones_de_escrituras_fk_id_persona_escribano_fkey|deed_managements_fk_id_notary_person_fkey",
            "deed_managements|gestiones_de_escrituras_pkey|deed_managements_pkey",
            "deeds|escrituras_pkey|deeds_pkey",
            "document_cost_templates|plantilla_costos_documento_fk_id_tipo_documento_fkey|document_cost_templates_fk_id_document_type_fkey",
            "document_cost_templates|plantilla_costos_documento_fk_id_tipo_tramite_fkey|document_cost_templates_fk_id_procedure_type_fkey",
            "document_cost_templates|plantilla_costos_documento_pkey|document_cost_templates_pkey",
            "document_types|tipos_de_documento_pkey|document_types_pkey",
            "folio_copies|folios_copias_fk_id_copia_fkey|folio_copies_fk_id_copy_fkey",
            "folio_copies|folios_copias_fk_id_folio_fkey|folio_copies_fk_id_folio_fkey",
            "folio_copies|folios_copias_pkey|folio_copies_pkey",
            "folios|folios_fk_id_cuaderno_fkey|folios_fk_id_notebook_fkey",
            "folios|folios_fk_id_escritura_fkey|folios_fk_id_deed_fkey",
            "folios|folios_fk_id_persona_escribano_fkey|folios_fk_id_notary_person_fkey",
            "folio_types|tipos_de_folio_pkey|folio_types_pkey",
            "history|historial_fk_id_estado_gestion_fkey|history_fk_id_management_status_fkey",
            "history|historial_fk_id_gestion_fkey|history_fk_id_deed_management_fkey",
            "history|historial_pkey|history_pkey",
            "identification_types|tipos_identificacion_pkey|identification_types_pkey",
            "items|items_fk_id_presupuesto_fkey|items_fk_id_budget_fkey",
            "management_statuses|estados_de_gestion_pkey|management_statuses_pkey",
            "notebooks|cuadernos_fk_id_persona_escribano_fkey|notebooks_fk_id_notary_person_fkey",
            "notebooks|cuadernos_pkey|notebooks_pkey",
            "notebooks|uq_cuaderno_numero_anio_escribano|notebooks_number_year_number_fk_id_notary_person_key",
            "payments|pagos_fk_id_presupuesto_fkey|payments_fk_id_budget_fkey",
            "payments|pagos_pkey|payments_pkey",
            "people|personas_fk_id_tipo_identificacion_fkey|people_fk_id_identification_type_fkey",
            "people|personas_pkey|people_pkey",
            "person_procedures|tramites_personas_fk_id_persona_cliente_fkey|person_procedures_fk_id_client_person_fkey",
            "person_procedures|tramites_personas_fk_id_tramite_fkey|person_procedures_fk_id_procedure_fkey",
            "person_procedures|tramites_personas_pkey|person_procedures_pkey",
            "procedure_folders|carpetas_tramite_fk_id_gestion_fkey|procedure_folders_fk_id_deed_management_fkey",
            "procedure_folders|carpetas_tramite_fk_id_tramite_fkey|procedure_folders_fk_id_procedure_fkey",
            "procedure_folders|carpetas_tramite_fk_id_tramite_key|procedure_folders_fk_id_procedure_key",
            "procedure_folders|carpetas_tramite_pkey|procedure_folders_pkey",
            "procedure_folders|uq_carpeta_tramite_numero|procedure_folders_number_key",
            "procedures|fk_tramites_presupuesto|procedures_fk_id_budget_fkey",
            "procedures|tramites_fk_id_escritura_fkey|procedures_fk_id_deed_fkey",
            "procedures|tramites_fk_id_gestion_fkey|procedures_fk_id_deed_management_fkey",
            "procedures|tramites_fk_id_inmueble_fkey|procedures_fk_id_property_fkey",
            "procedures|tramites_fk_id_tipo_tramite_fkey|procedures_fk_id_procedure_type_fkey",
            "procedures|tramites_pkey|procedures_pkey",
            "procedure_templates|plantilla_tramites_fk_id_tipo_documento_fkey|procedure_templates_fk_id_document_type_fkey",
            "procedure_templates|plantilla_tramites_fk_id_tipo_tramite_fkey|procedure_templates_fk_id_procedure_type_fkey",
            "procedure_templates|plantilla_tramites_pkey|procedure_templates_pkey",
            "procedure_types|tipos_de_tramite_fk_workflow_definition_id_fkey|procedure_types_fk_workflow_definition_id_fkey",
            "procedure_types|tipos_de_tramite_pkey|procedure_types_pkey",
            "properties|inmuebles_pkey|properties_pkey",
            "registration_drafts|minutas_inscripcion_fk_id_escritura_fkey|registration_drafts_fk_id_deed_fkey",
            "registration_drafts|minutas_inscripcion_fk_id_escritura_key|registration_drafts_fk_id_deed_key",
            "registration_drafts|minutas_inscripcion_pkey|registration_drafts_pkey",
            "roles|roles_nombre_unique|roles_name_key",
            "submitted_documents|documentos_presentados_fk_id_tipo_documento_fkey|submitted_documents_fk_id_document_type_fkey",
            "submitted_documents|documentos_presentados_fk_id_tramite_fkey|submitted_documents_fk_id_procedure_fkey",
            "submitted_documents|documentos_presentados_pkey|submitted_documents_pkey",
            "substitutions|suplencias_fk_id_suplantado_fkey|substitutions_fk_id_substituted_person_fkey",
            "substitutions|suplencias_fk_id_suplente_fkey|substitutions_fk_id_substitute_person_fkey",
            "substitutions|suplencias_pkey|substitutions_pkey",
            "testimonies|testimonios_fk_id_escritura_fkey|testimonies_fk_id_deed_fkey",
            "testimonies|testimonios_pkey|testimonies_pkey",
            "testimony_movements|movimientos_testimonio_fk_id_testimonio_fkey|testimony_movements_fk_id_testimony_fkey",
            "testimony_movements|movimientos_testimonio_pkey|testimony_movements_pkey",
            "users|usuarios_fk_id_persona_fkey|users_fk_id_person_fkey",
            "users|usuarios_fk_id_rol_fkey|users_fk_id_role_fkey",
            "users|usuarios_pkey|users_pkey",
            "workflow_node|workflow_node_fk_estado_gestion_id_fkey|workflow_node_fk_management_status_id_fkey"
    );

    private static final Pattern SPANISH = Pattern.compile(
            "gestion|tramite|usuario|presupuesto|inmueble|persona|escritura|identificacion|nombre|apellido|"
                    + "cuaderno|minuta|suplencia|documento|copia|pago|historial|testimonio|carpeta|plantilla|"
                    + "escribano|suplantado|suplente|tipos_");

    @Test
    @DisplayName("Should rename leftover Spanish constraint names in a new migration")
    void shouldRenameSpanishConstraintNames() throws IOException {
        Path migration = repoRoot().resolve(
                "backend-api/src/main/resources/db/migration/V46__rename_spanish_constraint_names.sql");
        assertThat(migration).exists();
        String sql = Files.readString(migration);
        List<String> missing = new ArrayList<>();
        List<String> spanishTargets = new ArrayList<>();
        for (String row : RENAMES) {
            String[] parts = row.split("\\|");
            String statement = "ALTER TABLE " + parts[0] + " RENAME CONSTRAINT " + parts[1] + " TO " + parts[2];
            if (!sql.contains(statement)) {
                missing.add(statement);
            }
            if (SPANISH.matcher(parts[2]).find()) {
                spanishTargets.add(parts[2]);
            }
        }
        assertThat(missing).isEmpty();
        assertThat(spanishTargets).isEmpty();
    }

    private static Path repoRoot() {
        Path cursor = Path.of("").toAbsolutePath();
        while (cursor != null && !Files.isDirectory(cursor.resolve("backend-api"))) {
            cursor = cursor.getParent();
        }
        if (cursor == null) {
            throw new IllegalStateException("Repository root not found");
        }
        return cursor;
    }
}
