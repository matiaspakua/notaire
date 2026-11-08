/**
 * Outbound port for the concept catalog (#1781 / ADR-030).
 */
import type { Concept } from "../domain/concept";

/** Write payload for create/update. */
export interface ConceptInput {
  name: string;
  value?: number;
  percentage?: number;
  fixed?: boolean;
  enabled?: boolean;
  version?: number;
}

export interface ConceptGateway {
  list(): Promise<Concept[]>;
  searchByName(name: string): Promise<Concept[]>;
  create(input: ConceptInput): Promise<Concept>;
  update(id: number, input: ConceptInput): Promise<Concept>;
  delete(id: number): Promise<void>;
  isInUse(id: number): Promise<boolean>;
}
