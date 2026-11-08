import type { SubstitutionGateway } from "./ports";
import type { SubstitutionInput } from "../domain/substitution";

export function createSubstitutionUseCases(gateway: SubstitutionGateway) {
  return {
    list: () => gateway.list(),
    create: (input: SubstitutionInput) => gateway.create(input),
    update: (id: number, input: SubstitutionInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type SubstitutionUseCases = ReturnType<typeof createSubstitutionUseCases>;
