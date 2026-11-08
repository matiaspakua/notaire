import { budgetTemplateHttpGateway } from "@/adapters/http/budget-template/budget-template-http-gateway";
import { createBudgetTemplateUseCases } from "@/core/budget-template/application/use-cases";

export const budgetTemplateUseCases = createBudgetTemplateUseCases(budgetTemplateHttpGateway);
