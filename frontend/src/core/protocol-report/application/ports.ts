/**
 * Outbound port for protocol PDF reports (#1833 / ADR-030).
 */
export interface ProtocolReportGateway {
  /** Download a report PDF for an absolute API path under /api/v1 (e.g. /reports/...). */
  download(path: string): Promise<Blob>;
}
