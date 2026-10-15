/**
 * Outbound port for the identification-type catalog (#1732 / ADR-030).
 */
import type { IdentificationType } from "../domain/identification-type";

/** Write payload matching IdentificationTypeRequest (`characters` is digit length). */
export interface IdentificationTypeInput {
  name: string;
  characters: string;
}

export interface IdentificationTypeGateway {
  list(): Promise<IdentificationType[]>;
  create(input: IdentificationTypeInput): Promise<IdentificationType>;
  update(id: number, input: IdentificationTypeInput): Promise<IdentificationType>;
  delete(id: number): Promise<void>;
}
