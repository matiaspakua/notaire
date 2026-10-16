import type { PropertyGateway } from "./ports";
import type { PropertyInput } from "../domain/property";

export function createPropertyUseCases(gateway: PropertyGateway) {
  return {
    list: () => gateway.list(),
    create: (input: PropertyInput) => gateway.create(input),
    update: (id: number, input: PropertyInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type PropertyUseCases = ReturnType<typeof createPropertyUseCases>;
