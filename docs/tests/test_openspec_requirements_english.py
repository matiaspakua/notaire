#!/usr/bin/env python3
"""Living OpenSpec requirement prose is English. Spec ids and stored labels stay."""

import unittest
from pathlib import Path

SPECS = (
    Path(__file__).resolve().parents[1]
    / "openspec"
    / "specs"
)


class OpenSpecRequirementsEnglishTest(unittest.TestCase):
    def test_spec_ids_stay(self):
        for spec_id in (
            "escritura-firma",
            "presupuesto-plantilla",
            "presupuesto-catalogo-items",
            "pago-recibo",
            "persona-credencial-escribano",
            "copia-validacion-testimonio-inscripto",
            "documento-presentado-herencia-vencimiento",
            "costos-documentos-presupuesto",
            "numeracion-escrituras",
            "folio-vinculacion-escritura",
            "testimonio-generacion-verificacion",
            "protocolo-auxiliar",
            "pagos-parciales-cuotas",
            "carpetas-de-tramite",
            "descuentos-recargos-presupuesto",
            "persona-validacion-duplicados",
            "gestion-workflow-transicion",
            "gestion-bitacora",
            "minuta-inscripcion",
            "testimonio-movimiento-inscripcion",
            "tipo-documento-vencimiento-config",
            "cuadernos-de-folios",
            "api-authorization",
            "pago-presupuesto-gestion-summary",
            "presupuesto-tramite-relation",
            "gestion-asignacion-suplencia",
            "deed-management-entity-null-safety",
            "gestion-archive-debt-check",
            "pago-limite-saldo-pendiente",
            "pago-presupuesto-picker-saldo",
            "pagos",
            "presupuesto-from-template",
            "procedure-fk-hydration",
            "inmueble-valuacion-fiscal",
            "submitted-document-update",
            "testimony-reentry-data",
            "budget-person-response",
            "backend-test-isolation",
            "admin-route-guard",
            "dashboard-nav-canonical-routes",
            "next-middleware-to-proxy",
        ):
            path = SPECS / spec_id / "spec.md"
            text = path.read_text(encoding="utf-8")
            self.assertTrue(text.startswith(f"# {spec_id} Specification"), spec_id)

    def test_requirement_prose_is_english_and_keeps_stored_labels(self):
        firma = (SPECS / "escritura-firma" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("### Requirement: Sign a deed only when it is Sin Firmar and has a folio", firma)
        self.assertIn('"Sin Firmar"', firma)
        self.assertIn('"Firmada"', firma)
        self.assertIn("Escribano", firma)
        self.assertIn("CU06", firma)
        plantilla = (SPECS / "presupuesto-plantilla" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", plantilla)
        self.assertIn("CU39", plantilla)
        catalog = (SPECS / "presupuesto-catalogo-items" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("CU71", catalog)
        receipt = (SPECS / "pago-recibo" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("{idPayment}", receipt)
        self.assertIn("recibo_pago_", receipt)
        self.assertIn("`amount`", receipt)
        credential = (SPECS / "persona-credencial-escribano" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`notaryRegistrationNumber`", credential)
        self.assertIn("CU48", credential)
        self.assertIn("CU51", credential)
        copy = (SPECS / "copia-validacion-testimonio-inscripto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`registered = true`", copy)
        self.assertIn("CU87", copy)
        submitted = (SPECS / "documento-presentado-herencia-vencimiento" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`expires`", submitted)
        self.assertIn("`dueDays`", submitted)
        self.assertIn("`deliveredBy`", submitted)
        self.assertIn("`dateEntry`", submitted)
        self.assertIn("`dateDue`", submitted)
        self.assertIn("CU42", submitted)
        costs = (SPECS / "costos-documentos-presupuesto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`amountToPay`", costs)
        numbering = (SPECS / "numeracion-escrituras" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Protocolo Auxiliar", numbering)
        self.assertIn("Protocolo Principal", numbering)
        self.assertIn("no pasó", numbering)
        self.assertIn("CU86", numbering)
        folio = (SPECS / "folio-vinculacion-escritura" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`deedId`", folio)
        self.assertIn('"Utilizado"', folio)
        self.assertIn("GET /api/v1/folios/{id}", folio)
        testimony = (SPECS / "testimonio-generacion-verificacion" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Firmada"', testimony)
        self.assertIn("Escribano", testimony)
        self.assertIn("CU07", testimony)
        auxiliary = (SPECS / "protocolo-auxiliar" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Protocolo Auxiliar", auxiliary)
        self.assertIn("Protocolo Principal", auxiliary)
        self.assertIn("`isAuxiliary`", auxiliary)
        self.assertIn("GET /api/v1/auxiliary-protocol/available-folios", auxiliary)
        self.assertIn("`idFolio`", auxiliary)
        self.assertIn("CU81", auxiliary)
        payments = (SPECS / "pagos-parciales-cuotas" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`NoPayments`", payments)
        self.assertIn("`PARTIAL`", payments)
        self.assertIn("`PAID`", payments)
        self.assertIn("GET /api/v1/payments/budget/{id}/status", payments)
        self.assertIn("`amount`", payments)
        self.assertIn("CU15", payments)
        self.assertIn("CU47", payments)
        folders = (SPECS / "carpetas-de-tramite" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Activa"', folders)
        self.assertIn('"Espera"', folders)
        self.assertIn('"Archivada"', folders)
        self.assertIn("`confirmado`", folders)
        self.assertIn("CU85", folders)
        self.assertIn("CU16", folders)
        adjustments = (SPECS / "descuentos-recargos-presupuesto" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"NORMAL"', adjustments)
        self.assertIn('"DESCUENTO"', adjustments)
        self.assertIn('"RECARGO"', adjustments)
        self.assertIn("`item_type`", adjustments)
        self.assertIn("`reason`", adjustments)
        self.assertIn("CU45", adjustments)
        self.assertIn("CU71", adjustments)
        duplicates = (SPECS / "persona-validacion-duplicados" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`identificationNumber`", duplicates)
        self.assertIn("fk_id_identification_type", duplicates)
        self.assertIn("existingPersonId", duplicates)
        self.assertIn("CU17", duplicates)
        self.assertIn("#799", duplicates)
        self.assertIn("#835", duplicates)
        workflow = (SPECS / "gestion-workflow-transicion" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`ProcedureType`", workflow)
        self.assertIn("POST /api/v1/managements/{id}/transition", workflow)
        self.assertIn("`managementStatusId`", workflow)
        self.assertIn("GET /api/v1/managements/{id}/workflow-trace", workflow)
        self.assertIn("`originNodeId`", workflow)
        self.assertIn("`currentStatus`", workflow)
        self.assertIn("CU83", workflow)
        history = (SPECS / "gestion-bitacora" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`ManagementHistory`", history)
        self.assertIn('"Archivada"', history)
        self.assertIn("GET /api/v1/managements/{id}/history", history)
        self.assertIn("GET /api/v1/managements/{id}/current-status", history)
        self.assertIn("CU13", history)
        draft = (SPECS / "minuta-inscripcion" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Generada"', draft)
        self.assertIn('"Presentado para inscripción"', draft)
        self.assertIn('"Observado"', draft)
        self.assertIn('"Inscripto"', draft)
        self.assertIn("POST /api/v1/registration-drafts", draft)
        self.assertIn("`registrationNumber`", draft)
        self.assertIn("`volumeFolioLandRecord`", draft)
        self.assertIn("`boundaries`", draft)
        self.assertIn("`cadastralDesignation`", draft)
        self.assertIn("minuta_inscripcion_", draft)
        self.assertIn("CU82", draft)
        movement = (SPECS / "testimonio-movimiento-inscripcion" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`dateEntry`", movement)
        self.assertIn("`dateExit`", movement)
        self.assertIn("`dateRegistration`", movement)
        self.assertIn("`registered`", movement)
        self.assertIn("`cardNumber`", movement)
        self.assertIn("Escribano", movement)
        self.assertIn("POST /api/v1/testimony-movements/{idTestimony}/enter-registration", movement)
        self.assertIn("CU11", movement)
        self.assertIn("CU12", movement)
        self.assertIn("CU44", movement)
        document_type = (SPECS / "tipo-documento-vencimiento-config" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`expires`", document_type)
        self.assertIn("`dueDays`", document_type)
        self.assertIn("`deliveredBy`", document_type)
        self.assertIn("`enabled`", document_type)
        self.assertIn("`returned`", document_type)
        self.assertIn("CU27", document_type)
        self.assertIn("CU32", document_type)
        notebook = (SPECS / "cuadernos-de-folios" / "spec.md").read_text(encoding="utf-8")
        self.assertIn('"Asignado a cuaderno"', notebook)
        self.assertIn('"Errose"', notebook)
        self.assertIn("no pasó", notebook)
        self.assertIn("`idsFolio`", notebook)
        self.assertIn("`idNotary`", notebook)
        self.assertIn("GET /api/v1/notebooks/{id}/cover", notebook)
        self.assertIn("notebook_", notebook)
        self.assertIn("Escribano", notebook)
        self.assertIn("CU80", notebook)
        combined = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "escritura-firma",
                "presupuesto-plantilla",
                "presupuesto-catalogo-items",
                "pago-recibo",
                "persona-credencial-escribano",
                "copia-validacion-testimonio-inscripto",
                "documento-presentado-herencia-vencimiento",
                "costos-documentos-presupuesto",
                "numeracion-escrituras",
                "folio-vinculacion-escritura",
                "testimonio-generacion-verificacion",
            )
        )
        for retired in (
            "El sistema SHALL",
            "tipo de trámite",
            "`Presupuesto`",
            "idPago",
            "registroEscribano",
            "inscripta",
            "vence",
            "diasVencimiento",
            "quienEntrega",
            "fechaVencimiento",
            "fechaIngreso",
            "importeAPagar",
            "escrituraId",
        ):
            self.assertNotIn(retired, combined, retired)
        later = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "protocolo-auxiliar",
                "pagos-parciales-cuotas",
                "carpetas-de-tramite",
                "descuentos-recargos-presupuesto",
                "persona-validacion-duplicados",
            )
        )
        for retired in (
            "El sistema SHALL",
            "numeroIdentificacion",
            "fk_id_tipo_identificacion",
        ):
            self.assertNotIn(retired, later, retired)
        circuit = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "gestion-workflow-transicion",
                "gestion-bitacora",
                "minuta-inscripcion",
                "testimonio-movimiento-inscripcion",
                "tipo-documento-vencimiento-config",
                "cuadernos-de-folios",
            )
        )
        for retired in (
            "El sistema SHALL",
            "tipo de trámite",
            "bitácora",
            "gestión",
            "vence",
            "diasVencimiento",
            "quienEntrega",
        ):
            self.assertNotIn(retired, circuit, retired)
        auth = (SPECS / "api-authorization" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Escribano", auth)
        self.assertIn("Administrador", auth)
        self.assertIn("EMPLEADO", auth)
        self.assertIn("ESCRIBANO", auth)
        self.assertIn("/api/v1/procedure-types", auth)
        self.assertIn("/api/v1/concepts", auth)
        self.assertIn("/api/v1/management-statuses", auth)
        self.assertIn("CU78", auth)
        payment_summary = (SPECS / "pago-presupuesto-gestion-summary" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`idBudget`", payment_summary)
        self.assertIn("`pendingBalance`", payment_summary)
        self.assertIn("`managementHeading`", payment_summary)
        self.assertIn("GET /api/v1/budgets/{id}/summary", payment_summary)
        self.assertIn("GET /api/v1/managements/{id}/financial-summary", payment_summary)
        self.assertIn("`Budget`", payment_summary)
        self.assertIn("CU47", payment_summary)
        relation = (SPECS / "presupuesto-tramite-relation" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", relation)
        self.assertIn("`Procedure`", relation)
        self.assertIn("fk_id_budget", relation)
        self.assertIn("`procedureList`", relation)
        self.assertIn("CU02", relation)
        self.assertIn("CU45", relation)
        substitution = (SPECS / "gestion-asignacion-suplencia" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`fkIdSubstitute`", substitution)
        self.assertIn("`notaryPersonId`", substitution)
        self.assertIn("Management redirected by active substitution", substitution)
        self.assertIn("POST /api/v1/managements", substitution)
        self.assertIn("CU22", substitution)
        null_safety = (SPECS / "deed-management-entity-null-safety" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`getDto()`", null_safety)
        self.assertIn("`getDtoNotary()`", null_safety)
        self.assertIn("`personNotary`", null_safety)
        self.assertIn("`fkIdIdentificationType`", null_safety)
        self.assertIn("DeedManagementEntityTest", null_safety)
        archive = (SPECS / "gestion-archive-debt-check" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`pendingBalance`", archive)
        self.assertIn("`pendingDebtAtArchiving`", archive)
        self.assertIn("`confirmado`", archive)
        self.assertIn('"Archivada"', archive)
        self.assertIn("`ProcedureType`", archive)
        self.assertIn("GET /api/v1/managements/{id}/outstanding-balance", archive)
        self.assertIn("POST /api/v1/managements/{id}/archive", archive)
        self.assertIn("CU16", archive)
        self.assertIn("CU83", archive)
        finance = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "api-authorization",
                "pago-presupuesto-gestion-summary",
                "presupuesto-tramite-relation",
                "gestion-asignacion-suplencia",
                "deed-management-entity-null-safety",
                "gestion-archive-debt-check",
            )
        )
        for retired in (
            "El sistema SHALL",
            "tipo de trámite",
            "tipo-tramite",
            "gestión",
            "trámites",
            "setAtributos",
            "saldo pendiente",
        ):
            self.assertNotIn(retired, finance, retired)
        limit = (SPECS / "pago-limite-saldo-pendiente" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`amount`", limit)
        self.assertIn("`pendingBalance`", limit)
        self.assertIn("`Budget`", limit)
        self.assertIn("409", limit)
        self.assertIn("CU15", limit)
        picker = (SPECS / "pago-presupuesto-picker-saldo" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", picker)
        self.assertIn("`pendingBalance`", picker)
        self.assertIn("saldoExcedido", picker)
        self.assertIn("CU15", picker)
        payments = (SPECS / "pagos" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`paymentMethod`", payments)
        self.assertIn('"Efectivo"', payments)
        self.assertIn("`Budget`", payments)
        self.assertIn("POST /api/v1/payments", payments)
        self.assertIn("Procesar pago", payments)
        template = (SPECS / "presupuesto-from-template" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", template)
        self.assertIn("`Item`", template)
        self.assertIn("POST /api/v1/budgets/{id}/items-from-template", template)
        self.assertIn("`procedureTypeId`", template)
        self.assertIn("CU01", template)
        hydration = (SPECS / "procedure-fk-hydration" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`idBudget`", hydration)
        self.assertIn('"Firmada"', hydration)
        self.assertIn("POST /api/v1/procedures", hydration)
        self.assertIn("CU82", hydration)
        appraisal = (SPECS / "inmueble-valuacion-fiscal" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`fiscalAppraisal`", appraisal)
        self.assertIn("fiscal_valuation", appraisal)
        self.assertIn("POST /api/v1/properties", appraisal)
        self.assertIn("Gestión de Inmuebles", appraisal)
        collection = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "pago-limite-saldo-pendiente",
                "pago-presupuesto-picker-saldo",
                "pagos",
                "presupuesto-from-template",
                "procedure-fk-hydration",
                "inmueble-valuacion-fiscal",
            )
        )
        for retired in (
            "El sistema SHALL",
            "`monto`",
            "`Presupuesto`",
            "metodoPago",
            "valuacionFiscal",
            "trámite",
            "saldo pendiente",
        ):
            self.assertNotIn(retired, collection, retired)
        submitted = (SPECS / "submitted-document-update" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`procedureId`", submitted)
        self.assertIn("`delivered`", submitted)
        self.assertIn("PUT /api/v1/submitted-documents/{id}", submitted)
        self.assertIn("CU72", submitted)
        reentry = (SPECS / "testimony-reentry-data" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`cardNumber`", reentry)
        self.assertIn("`observedByRegistry`", reentry)
        self.assertIn("reingresar", reentry)
        self.assertIn("btn-reenter-testimony", reentry)
        self.assertIn("CU44", reentry)
        budget_person = (SPECS / "budget-person-response" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", budget_person)
        self.assertIn("`personId`", budget_person)
        self.assertIn("`lastName`", budget_person)
        self.assertIn("POST /api/v1/budgets", budget_person)
        isolation = (SPECS / "backend-test-isolation" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`Budget`", isolation)
        self.assertIn("`amount`", isolation)
        self.assertIn("`pendingBalance`", isolation)
        self.assertIn("`idBudget`", isolation)
        self.assertIn("CU15", isolation)
        guard = (SPECS / "admin-route-guard" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("/dashboard/administration", guard)
        self.assertIn("ADMINISTRADOR", guard)
        self.assertIn("ESCRIBANO", guard)
        self.assertIn("EMPLEADO", guard)
        self.assertIn("CU78", guard)
        nav = (SPECS / "dashboard-nav-canonical-routes" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`suplencias`", nav)
        self.assertIn("`reportes`", nav)
        self.assertIn("`auditoria`", nav)
        self.assertIn("nav-suplencias", nav)
        self.assertIn("nav-reportes", nav)
        self.assertIn("/dashboard/substitutions", nav)
        self.assertIn("/dashboard/reports", nav)
        proxy = (SPECS / "next-middleware-to-proxy" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("frontend/src/proxy.ts", proxy)
        self.assertIn("Login al sistema", proxy)
        self.assertIn("/dashboard/administration", proxy)
        self.assertIn("CU84", proxy)
        screens = "\n".join(
            (SPECS / spec_id / "spec.md").read_text(encoding="utf-8")
            for spec_id in (
                "submitted-document-update",
                "testimony-reentry-data",
                "budget-person-response",
                "backend-test-isolation",
                "admin-route-guard",
                "dashboard-nav-canonical-routes",
                "next-middleware-to-proxy",
            )
        )
        for retired in (
            "trámite",
            "cartón",
            "presupuesto",
            "`monto`",
            "saldo pendiente",
            "administración",
            "Auditoría",
            "Suplencias",
            "Reportes",
        ):
            self.assertNotIn(retired, screens, retired)

    def test_leftover_titles_name_the_live_contract(self):
        i18n = (SPECS / "frontend-i18n-page-coverage" / "spec.md").read_text(encoding="utf-8")
        self.assertIn(
            "#### Scenario: Substitutions, reports, roles, and items use translations",
            i18n,
        )
        self.assertIn(
            "#### Scenario: Audit all-modules filter is translated",
            i18n,
        )
        for key in ("`suplencias`", "`reportes`", "`auditoria`", "`administracion`"):
            self.assertIn(key, i18n, key)
        self.assertNotIn("#### Scenario: Suplencias", i18n)
        self.assertNotIn("#### Scenario: Auditoria", i18n)

        icons = (SPECS / "icon-button-accessible-names" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("#### Scenario: Budget summary action is named", icons)
        self.assertIn("icon-only budget summary", icons)
        self.assertIn("“Editar”", icons)
        self.assertIn("“Eliminar”", icons)
        self.assertIn("/editar/i", icons)
        self.assertIn("personas", icons)
        self.assertIn("administracion", icons)
        self.assertIn("tramites", icons)
        self.assertNotIn("presupuesto summary", icons)
        self.assertNotIn("Presupuesto resumen", icons)

        load = (SPECS / "k6-load-test-restore" / "spec.md").read_text(encoding="utf-8")
        self.assertIn(
            "#### Scenario: Script covers managements, budgets, and procedures with Bearer auth",
            load,
        )
        self.assertIn("/api/v1/managements", load)
        self.assertIn("/api/v1/budgets", load)
        self.assertIn("/api/v1/procedures", load)
        self.assertNotIn("gestiones presupuestos", load)

        reports = (SPECS / "report-pdfs" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("Payment pending", reports)
        self.assertNotIn("Payment pendiente", reports)

        binding = (SPECS / "api-request-dto-binding" / "spec.md").read_text(encoding="utf-8")
        self.assertIn("`AuditAspect`", binding)
        self.assertNotIn("AuditoriaAspect", binding)

        sad = (
            Path(__file__).resolve().parents[1]
            / "200-architecture"
            / "201-SAD"
            / "sad.md"
        ).read_text(encoding="utf-8")
        self.assertIn("**Live status**", sad)
        self.assertIn("Escribano", sad)
        self.assertNotIn("Estado vivo", sad)


if __name__ == "__main__":
    unittest.main()
