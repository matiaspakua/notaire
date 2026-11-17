import type { Copy, CopyInput } from "../domain/copy";

export interface CopyPageRequest {
  page: number;
  size: number;
  sort: string;
}

export interface CopyPage {
  content: Copy[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CopyGateway {
  list(): Promise<Copy[]>;
  listPage(request: CopyPageRequest): Promise<CopyPage>;
  create(input: CopyInput): Promise<void>;
  update(id: number, input: CopyInput): Promise<void>;
  delete(id: number): Promise<void>;
}
