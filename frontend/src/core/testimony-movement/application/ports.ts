/**
 * Outbound port for testimony movements (#1834 / ADR-030).
 */
import type { ReenterInput, TestimonyMovement, WithdrawInput } from "../domain/testimony-movement";

export interface TestimonyMovementGateway {
  enterRegistration(idTestimony: number): Promise<TestimonyMovement>;
  registerInscription(idTestimony: number): Promise<TestimonyMovement>;
  withdraw(input: WithdrawInput): Promise<TestimonyMovement>;
  reenter(input: ReenterInput): Promise<TestimonyMovement>;
}
