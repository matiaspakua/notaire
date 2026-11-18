/**
 * Outbound port for submitted documents (#1841 / ADR-030).
 */
import type {
  SubmittedDocument,
  SubmittedDocumentCreateInput,
  SubmittedDocumentUpdateInput,
  UpcomingExpiration,
} from "../domain/submitted-document";

export interface SubmittedDocumentPageRequest {
  page: number;
  size: number;
  sort: string;
}

export interface SubmittedDocumentPage {
  content: SubmittedDocument[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface SubmittedDocumentGateway {
  list(): Promise<SubmittedDocument[]>;
  listPage(request: SubmittedDocumentPageRequest): Promise<SubmittedDocumentPage>;
  create(input: SubmittedDocumentCreateInput): Promise<SubmittedDocument>;
  update(id: number, input: SubmittedDocumentUpdateInput): Promise<void>;
  delete(id: number): Promise<void>;
  listUpcomingExpirations(days: number): Promise<UpcomingExpiration[]>;
}
