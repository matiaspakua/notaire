import type { CopyGateway } from "./ports";
import type { CopyInput } from "../domain/copy";
import type { CopyPageRequest } from "./ports";

export function createCopyUseCases(gateway: CopyGateway) {
  return {
    list: () => gateway.list(),
    listPage: (request: CopyPageRequest) => gateway.listPage(request),
    create: (input: CopyInput) => gateway.create(input),
    update: (id: number, input: CopyInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
  };
}

export type CopyUseCases = ReturnType<typeof createCopyUseCases>;
