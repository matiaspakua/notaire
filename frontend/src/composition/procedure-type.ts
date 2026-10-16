/**
 * Composition root for the procedure-type catalog (#1796 / ADR-030).
 */
import { procedureTypeHttpGateway } from "@/adapters/http/procedure-type/procedure-type-http-gateway";
import { createProcedureTypeUseCases } from "@/core/procedure-type/application/use-cases";

export const procedureTypeUseCases = createProcedureTypeUseCases(procedureTypeHttpGateway);
