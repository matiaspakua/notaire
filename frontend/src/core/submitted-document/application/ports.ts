/**
 * Outbound port for submitted documents (#1841 / ADR-030).
 */
import type {
  SubmittedDocument,
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
  UpcomingExpiration,
} from "../domain/submitted-document";

export interface SubmittedDocumentGateway {
  list(): Promise<SubmittedDocument[]>;
  create(input: SubmittedDocumentCreateInput): Promise<SubmittedDocument>;
  update(id: number, input: SubmittedDocumentUpdateInput): Promise<void>;
  delete(id: number): Promise<void>;
  listUpcomingExpirations(days: number): Promise<UpcomingExpiration[]>;
}
