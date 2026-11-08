import type {
  DocumentReentryInput,
  ManagementDocumentReentry,
  ReenteredDocument,
} from "../domain/document-reentry";

export interface DocumentReentryGateway {
  getByManagement(managementId: number): Promise<ManagementDocumentReentry>;
  reenter(managementId: number, input: DocumentReentryInput): Promise<ReenteredDocument>;
}
