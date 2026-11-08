/**
 * Outbound port for auxiliary protocol (#1833 / ADR-030).
 */
import type { AuxiliaryFolio, StartAuxiliaryDeedInput } from "../domain/auxiliary-protocol";

export interface AuxiliaryProtocolGateway {
  listAvailableFolios(): Promise<AuxiliaryFolio[]>;
  startDeed(input: StartAuxiliaryDeedInput): Promise<void>;
}
