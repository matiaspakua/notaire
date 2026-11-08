/**
 * HTTP adapter for the document-type catalog (#1777 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { DocumentType } from "@/core/document-type/domain/document-type";
import type {
  DocumentTypeGateway,
  DocumentTypeInput,
} from "@/core/document-type/application/ports";

/** Wire shape from DocumentType / DtoDocumentType. */
interface DocumentTypeDto {
  idDocumentType?: number;
  name?: string;
  expires?: boolean;
  dueDays?: number | null;
  deliveredBy?: string | null;
  enabled?: boolean;
  returned?: boolean;
}

function toDomain(dto: DocumentTypeDto): DocumentType {
  return {
    id: dto.idDocumentType ?? 0,
    name: dto.name ?? "",
    expires: dto.expires,
    dueDays: dto.dueDays,
    deliveredBy: dto.deliveredBy,
    enabled: dto.enabled,
    returned: dto.returned,
  };
}

function toWire(input: DocumentTypeInput): DocumentTypeDto {
  return {
    name: input.name,
    expires: input.expires,
    dueDays: input.dueDays,
    deliveredBy: input.deliveredBy,
    enabled: input.enabled,
    returned: input.returned,
  };
}

export const documentTypeHttpGateway: DocumentTypeGateway = {
  list: async () => {
    const rows = await apiGet<DocumentTypeDto[]>("/document-types");
    return rows.map(toDomain);
  },
  searchByName: async (name: string) => {
    const rows = await apiGet<DocumentTypeDto[]>(
      `/document-types/search?name=${encodeURIComponent(name)}`,
    );
    return rows.map(toDomain);
  },
  create: async (input: DocumentTypeInput) =>
    toDomain(await apiPost<DocumentTypeDto>("/document-types", toWire(input))),
  update: async (id: number, input: DocumentTypeInput) =>
    toDomain(await apiPut<DocumentTypeDto>(`/document-types/${id}`, toWire(input))),
  delete: async (id: number) => {
    await apiDelete(`/document-types/${id}`);
  },
  isInUse: async (id: number) => {
    const { inUse } = await apiGet<{ inUse: boolean }>(`/document-types/${id}/in-use`);
    return inUse;
  },
};
