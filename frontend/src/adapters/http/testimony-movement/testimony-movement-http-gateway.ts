/**
 * HTTP adapter for testimony movements (#1834 / ADR-030).
 */
import { apiPost } from "@/lib/api-client";
import type {
  ReenterInput,
  TestimonyMovement,
  WithdrawInput,
} from "@/core/testimony-movement/domain/testimony-movement";
import type { TestimonyMovementGateway } from "@/core/testimony-movement/application/ports";

export const testimonyMovementHttpGateway: TestimonyMovementGateway = {
  enterRegistration: async (idTestimony: number) =>
    apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/enter-registration`, {}),
  registerInscription: async (idTestimony: number) =>
    apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/register-inscription`, {}),
  withdraw: async ({ idTestimony, cardNumber }: WithdrawInput) =>
    apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/withdraw`, { cardNumber }),
  reenter: async ({ idTestimony, cardNumber, observedByRegistry, notes }: ReenterInput) =>
    apiPost<TestimonyMovement>(`/testimony-movements/${idTestimony}/reenter`, {
      cardNumber,
      observedByRegistry,
      notes,
    }),
};
