/**
 * Composition root for budgets (#1827 / ADR-030).
 */
import { budgetHttpGateway } from "@/adapters/http/budget/budget-http-gateway";
import { createBudgetUseCases } from "@/core/budget/application/use-cases";

export const budgetUseCases = createBudgetUseCases(budgetHttpGateway);
