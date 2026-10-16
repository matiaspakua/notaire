/**
 * React Query hooks for document types (CU27, CU65).
 * Hexagon wiring: #1777 / ADR-030.
 * Endpoint: GET/POST/PUT/DELETE /api/v1/document-types
 */
import { useQuery } from "@tanstack/react-query";
import { documentTypeUseCases } from "@/composition/document-type";
import type { DocumentType as DomainDocumentType } from "@/core/document-type/domain/document-type";
import type { DocumentType } from "@/types";

export const documentsKeys = {
  all: ["documentTypes"] as const,
  detail: (id: number) => ["documentTypes", id] as const,
};

/** Map domain model back to the wire-facing UI type used across the app. */
function toWire(doc: DomainDocumentType): DocumentType {
  return {
    idDocumentType: doc.id,
    name: doc.name,
    expires: doc.expires,
    dueDays: doc.dueDays,
    deliveredBy: doc.deliveredBy ?? undefined,
    enabled: doc.enabled,
    returned: doc.returned,
  };
}

export function useDocumentTypes() {
  return useQuery({
    queryKey: documentsKeys.all,
    queryFn: async () => (await documentTypeUseCases.list()).map(toWire),
  });
}
