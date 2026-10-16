/**
 * Outbound port for testimonies (#1834 / ADR-030).
 */
import type {
  Testimony,
  TestimonyCreateInput,
  TestimonyUpdateInput,
  TestimonyVerifyInput,
} from "../domain/testimony";

export interface TestimonyGateway {
  list(): Promise<Testimony[]>;
  generate(idDeed: number): Promise<Testimony>;
  create(input: TestimonyCreateInput): Promise<Testimony>;
  verify(id: number, input: TestimonyVerifyInput): Promise<Testimony>;
  update(id: number, input: TestimonyUpdateInput): Promise<void>;
  delete(id: number): Promise<void>;
  downloadCopy(idTestimony: number): Promise<Blob>;
}
