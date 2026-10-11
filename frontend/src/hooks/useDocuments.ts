/**
 * React Query hooks for document types (CU27, CU65).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/document-types
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { DocumentType } from "@/types";

export const documentsKeys = {
  all: ["documentTypes"] as const,
  detail: (id: number) => ["documentTypes", id] as const,
};

export function useDocumentTypes() {
  return useQuery({
    queryKey: documentsKeys.all,
    queryFn: () => apiGet<DocumentType[]>("/document-types"),
  });
}
