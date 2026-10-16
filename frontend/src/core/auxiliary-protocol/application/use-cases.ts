/**
 * Auxiliary protocol use cases (#1833 / ADR-030).
 */
import type { AuxiliaryProtocolGateway } from "./ports";
import type { StartAuxiliaryDeedInput } from "../domain/auxiliary-protocol";

export function createAuxiliaryProtocolUseCases(gateway: AuxiliaryProtocolGateway) {
  return {
    listAvailableFolios: () => gateway.listAvailableFolios(),
    startDeed: (input: StartAuxiliaryDeedInput) => gateway.startDeed(input),
  };
}

export type AuxiliaryProtocolUseCases = ReturnType<typeof createAuxiliaryProtocolUseCases>;
