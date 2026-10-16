import type { Property, PropertyInput } from "../domain/property";

export interface PropertyGateway {
  list(): Promise<Property[]>;
  create(input: PropertyInput): Promise<void>;
  update(id: number, input: PropertyInput): Promise<void>;
  delete(id: number): Promise<void>;
}
