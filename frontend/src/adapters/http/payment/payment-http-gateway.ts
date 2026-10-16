/**
 * HTTP adapter for payments (#1832 / ADR-030).
 * Only place in this catalog that may import api-client.
 */
import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import type { Payment, PaymentStatus } from "@/core/payment/domain/payment";
import type { PaymentGateway, PaymentInput } from "@/core/payment/application/ports";

/** Wire shape from DtoPaymentResponse / PaymentRequest — kept loose for OpenAPI drift. */
type PaymentDto = Payment;

function toDomain(dto: PaymentDto): Payment {
  return {
    idPayment: dto.idPayment,
    idBudget: dto.idBudget ?? dto.fkIdBudget?.idBudget,
    amount: dto.amount,
    date: dto.date,
    paymentMethod: dto.paymentMethod,
    notes: dto.notes,
    fkIdBudget: dto.fkIdBudget ? { idBudget: dto.fkIdBudget.idBudget } : undefined,
  };
}

function toCreateWire(input: PaymentInput): PaymentDto {
  return {
    idBudget: input.idBudget,
    amount: input.amount,
    date: input.date,
    paymentMethod: input.paymentMethod,
    notes: input.notes,
  };
}

function toUpdateWire(input: PaymentInput): PaymentDto {
  return {
    amount: input.amount,
    date: input.date,
    paymentMethod: input.paymentMethod,
    notes: input.notes,
  };
}

export const paymentHttpGateway: PaymentGateway = {
  list: async () => (await apiGet<PaymentDto[]>("/payments")).map(toDomain),
  listByBudget: async (idBudget: number) =>
    (await apiGet<PaymentDto[]>(`/payments/budget/${idBudget}`)).map(toDomain),
  listByDateRange: async (from: string, to: string) =>
    (
      await apiGet<PaymentDto[]>(
        `/payments/date?startDate=${encodeURIComponent(from)}&endDate=${encodeURIComponent(to)}`,
      )
    ).map(toDomain),
  getStatus: async (idBudget: number) =>
    apiGet<PaymentStatus>(`/payments/budget/${idBudget}/status`),
  getBalance: async (idBudget: number) => apiGet<number>(`/payments/budget/${idBudget}/balance`),
  create: async (input: PaymentInput) => {
    await apiPost<void>("/payments", toCreateWire(input));
  },
  update: async (id: number, input: PaymentInput) => {
    await apiPut<void>(`/payments/${id}`, toUpdateWire(input));
  },
  delete: async (id: number) => {
    await apiDelete(`/payments/${id}`);
  },
};
