/**
 * #1260 / #1197 P0.5 — high-traffic hooks consume OpenAPI-generated aliases.
 */
import { describe, expect, it } from "vitest";
import type { paths } from "@/types/api.generated";
import type {
  SubmittedDocument,
  DeedManagement,
  Budget,
  BudgetSummary,
} from "@/types/api";
import { managementsKeys } from "@/hooks/useManagements";
import { budgetsKeys } from "@/hooks/useBudgets";
import { submittedDocumentsKeys } from "@/hooks/useSubmittedDocuments";

describe("openapi generated types (#1260)", () => {
  it("exports schema aliases used by management, budget, and document hooks", () => {
    const management: DeedManagement = { idManagement: 1, procedureCount: 2 };
    const budget: Budget = { idBudget: 9, number: 1 };
    const summary: BudgetSummary = {
      idBudget: 9,
      numberBudget: 1,
      total: 100,
      pendingBalance: 0,
    };
    const doc: SubmittedDocument = { idSubmittedDocument: 3, delivered: false };
    expect(management.idManagement).toBe(1);
    expect(budget.idBudget).toBe(9);
    expect(summary.total).toBe(100);
    expect(doc.delivered).toBe(false);
  });

  it("keeps hook query-key factories for dashboard / list screens", () => {
    expect(managementsKeys.page({ page: 0, size: 1 })).toEqual(["managements", "page", { page: 0, size: 1 }]);
    expect(budgetsKeys.page({ page: 0, size: 1 })).toEqual([
      "budgets",
      "page",
      { page: 0, size: 1 },
    ]);
    expect(submittedDocumentsKeys.all).toEqual(["submittedDocuments"]);
  });
});

describe("openapi generated types: PDF report errors (#1593)", () => {
  it("types the budget report 500 as an ErrorResponse body", () => {
    type BudgetReport500 =
      paths["/api/v1/reports/budget/{idBudget}"]["get"]["responses"][500]["content"]["application/json"];
    const body: BudgetReport500 = { status: 500, message: "PDF failed" } as BudgetReport500;
    expect(body.message).toBe("PDF failed");
  });
});
