/**
 * Outbound port for deeds (#1831 / ADR-030).
 */
import type { Deed } from "../domain/deed";

export interface DeedPage {
  content: Deed[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface DeedPageRequest {
  page: number;
  size: number;
  sort?: string;
  /** Extra filters (e.g. status=Firmada for the signed picker). */
  params?: Record<string, string | number | undefined | null>;
}

export type DeedInput = Partial<Deed>;

export interface DeedGateway {
  getById(id: number): Promise<Deed>;
  listPage(request: DeedPageRequest): Promise<DeedPage>;
  searchByNumber(number: string): Promise<Deed[]>;
  create(input: DeedInput): Promise<void>;
  update(id: number, input: DeedInput): Promise<void>;
  delete(id: number): Promise<void>;
  /** CU06 — moves a deed from "Sin Firmar" to "Firmada" when a folio is assigned. */
  sign(id: number): Promise<Deed>;
}
