import type { CopyGateway } from "./ports";
import type { CopyInput } from "../domain/copy";

export function createCopyUseCases(gateway: CopyGateway) {
  return {
    list: () => gateway.list(),
    create: (input: CopyInput) => gateway.create(input),
    update: (id: number, input: CopyInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type CopyUseCases = ReturnType<typeof createCopyUseCases>;
