/**
 * Composition root for procedure templates (#1788 / ADR-030).
 */
import { procedureTemplateHttpGateway } from "@/adapters/http/procedure-template/procedure-template-http-gateway";
import { createProcedureTemplateUseCases } from "@/core/procedure-template/application/use-cases";

export const procedureTemplateUseCases = createProcedureTemplateUseCases(procedureTemplateHttpGateway);
