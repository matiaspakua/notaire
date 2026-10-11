/**
 * React Query hooks for DocumentType (CU27, CU65 — Tipos de documento).
 * Endpoint: GET/POST/PUT/DELETE /api/v1/document-types
 */
import { useQuery } from "@tanstack/react-query";
import { apiGet } from "@/lib/api-client";
import type { DocumentType } from "@/types";

export const documentsKeys = {
  all: ["tiposDocumento"] as const,
  detail: (id: number) => ["tiposDocumento", id] as const,
};

export function useDocumentTypes() {
  return useQuery({
    queryKey: documentsKeys.all,
    queryFn: () => apiGet<DocumentType[]>("/document-types"),
  });
}
