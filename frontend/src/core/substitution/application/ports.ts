import type { Substitution, SubstitutionInput } from "../domain/substitution";

export interface SubstitutionGateway {
  list(): Promise<Substitution[]>;
  create(input: SubstitutionInput): Promise<void>;
  update(id: number, input: SubstitutionInput): Promise<void>;
  delete(id: number): Promise<void>;
}
