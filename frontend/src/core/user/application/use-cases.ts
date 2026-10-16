import type { UserGateway } from "./ports";
import type { UserInput } from "../domain/user";

export function createUserUseCases(gateway: UserGateway) {
  return {
    list: () => gateway.list(),
    create: (input: UserInput) => gateway.create(input),
    update: (id: number, input: UserInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type UserUseCases = ReturnType<typeof createUserUseCases>;
