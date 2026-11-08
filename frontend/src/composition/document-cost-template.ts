import { documentCostTemplateHttpGateway } from "@/adapters/http/document-cost-template/document-cost-template-http-gateway";
import { createDocumentCostTemplateUseCases } from "@/core/document-cost-template/application/use-cases";

export const documentCostTemplateUseCases = createDocumentCostTemplateUseCases(
  documentCostTemplateHttpGateway,
);
