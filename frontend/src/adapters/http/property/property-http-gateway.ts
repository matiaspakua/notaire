/**
 * HTTP adapter for propertys (#1845 / ADR-030).
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Property, PropertyInput } from "@/core/property/domain/property";
import type { PropertyGateway } from "@/core/property/application/ports";

export const propertyHttpGateway: PropertyGateway = {
  list: () => apiGet<Property[]>("/properties"),
  create: async (input: PropertyInput) => { await apiPost<void>("/properties", input); },
  update: async (id, input) => { await apiPut<void>(`/properties/${id}`, input); },
  delete: async (id) => { await apiDelete(`/properties/${id}`); },
};
