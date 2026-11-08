import type { Copy, CopyInput } from "../domain/copy";

export interface CopyGateway {
  list(): Promise<Copy[]>;
  create(input: CopyInput): Promise<void>;
  update(id: number, input: CopyInput): Promise<void>;
  delete(id: number): Promise<void>;
}
