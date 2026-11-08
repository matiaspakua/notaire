/**
 * Composition root for protocol PDF reports (#1833 / ADR-030).
 */
import { protocolReportHttpGateway } from "@/adapters/http/protocol-report/protocol-report-http-gateway";
import { createProtocolReportUseCases } from "@/core/protocol-report/application/use-cases";

export const protocolReportUseCases = createProtocolReportUseCases(protocolReportHttpGateway);
