/**
 * Module names the backend writes to the audit log (`AuditModuleResolver`:
 * mapped controllers plus the controller-name fallback). The list is server
 * filtered (#1340), so the options can't be derived from the loaded page.
 * A module missing here is still listed and shown; it just isn't offered as
 * a filter until added. Follow-up: expose the set from the API.
 */
export const AUDIT_MODULES = [
  "AuxiliaryProtocol",
  "BudgetTemplates",
  "Budgets",
  "Concepts",
  "Copies",
  "DocumentCostTemplate",
  "DocumentTypes",
  "Documents",
  "Deeds",
  "FolioTypes",
  "Folios",
  "History",
  "IdentificationTypes",
  "Items",
  "ManagementStatuses",
  "Managements",
  "Notebook",
  "Payments",
  "People",
  "ProcedureFolder",
  "ProcedureTemplates",
  "ProcedureTypes",
  "Procedures",
  "Properties",
  "RegistrationDraft",
  "Reports",
  "Role",
  "Substitutions",
  "Testimonies",
  "TestimonyMovements",
  "Users",
  "WorkflowDefinition",
  "WorkflowNode",
  "WorkflowTransition",
  "WorkflowValidation",
] as const;
