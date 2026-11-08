/**
 * Testimony use cases (#1834 / ADR-030).
 */
import type { TestimonyGateway } from "./ports";
import type {
  TestimonyCreateInput,
  TestimonyUpdateInput,
  TestimonyVerifyInput,
} from "../domain/testimony";

export function createTestimonyUseCases(gateway: TestimonyGateway) {
  return {
    list: () => gateway.list(),
    generate: (idDeed: number) => gateway.generate(idDeed),
    create: (input: TestimonyCreateInput) => gateway.create(input),
    verify: (id: number, input: TestimonyVerifyInput) => gateway.verify(id, input),
    update: (id: number, input: TestimonyUpdateInput) => gateway.update(id, input),
    delete: (id: number) => gateway.delete(id),
    downloadCopy: (idTestimony: number) => gateway.downloadCopy(idTestimony),
  };
}

export type TestimonyUseCases = ReturnType<typeof createTestimonyUseCases>;
