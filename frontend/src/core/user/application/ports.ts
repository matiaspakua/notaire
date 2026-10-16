import type { UserAccount, UserInput } from "../domain/user";

export interface UserGateway {
  list(): Promise<UserAccount[]>;
  create(input: UserInput): Promise<UserAccount>;
  update(id: number, input: UserInput): Promise<void>;
  delete(id: number): Promise<void>;
}
