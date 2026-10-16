/**
 * Composition root for payments (#1832 / ADR-030).
 * Wires the HTTP gateway to core use cases once for the app.
 */
import { paymentHttpGateway } from "@/adapters/http/payment/payment-http-gateway";
import { createPaymentUseCases } from "@/core/payment/application/use-cases";

export const paymentUseCases = createPaymentUseCases(paymentHttpGateway);
