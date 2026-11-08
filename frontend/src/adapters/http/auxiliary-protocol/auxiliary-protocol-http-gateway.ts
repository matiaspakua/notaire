/**
 * HTTP adapter for auxiliary protocol (#1833 / ADR-030).
 */
import { apiGet, apiPost } from "@/lib/api-client";
import type { AuxiliaryFolio, StartAuxiliaryDeedInput } from "@/core/auxiliary-protocol/domain/auxiliary-protocol";
import type { AuxiliaryProtocolGateway } from "@/core/auxiliary-protocol/application/ports";

export const auxiliaryProtocolHttpGateway: AuxiliaryProtocolGateway = {
  listAvailableFolios: async () =>
    apiGet<AuxiliaryFolio[]>("/auxiliary-protocol/available-folios"),
  startDeed: async (input: StartAuxiliaryDeedInput) => {
    await apiPost("/auxiliary-protocol/deeds", input);
  },
};
