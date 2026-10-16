/**
 * Composition root for templates-page catalog lookups (#1788 / ADR-030).
 */
import { templatesCatalogLookups } from "@/adapters/http/templates/catalog-lookups-http-gateway";
import { documentTypeUseCases } from "@/composition/document-type";

export const templatesCatalog = {
  listProcedureTypes: templatesCatalogLookups.listProcedureTypes,
  listConcepts: templatesCatalogLookups.listConcepts,
  listDocumentTypes: () => documentTypeUseCases.list(),
};
