import { describe, it, expect } from "vitest";
import {
  EMPTY_DOCUMENT_FORM,
  missingDocumentFields,
  showsProcedureLink,
  toDocumentRequest,
} from "@/lib/submitted-document-form";

/**
 * CU04 / CU72 (#655): a submitted document is presented for a trámite of a
 * gestión, so the create form requires the document type and the trámite.
 */
describe("documento presentado form (#655)", () => {
  const complete = { ...EMPTY_DOCUMENT_FORM, tipoId: "3", fecha: "2026-09-05", managementId: "7", procedureId: "11" };

  it("requires the type and the trámite on create", () => {
    expect(missingDocumentFields(EMPTY_DOCUMENT_FORM, false)).toEqual(["tipoId", "tramiteId"]);
    expect(missingDocumentFields({ ...complete, procedureId: "" }, false)).toEqual(["tramiteId"]);
    expect(missingDocumentFields({ ...complete, tipoId: "" }, false)).toEqual(["tipoId"]);
    expect(missingDocumentFields(complete, false)).toEqual([]);
  });

  it("does not force a trámite when editing a legacy document without one", () => {
    expect(missingDocumentFields({ ...complete, managementId: "", procedureId: "" }, true)).toEqual([]);
  });

  it("offers the gestión/trámite link on create and on documents that have none yet", () => {
    expect(showsProcedureLink(null)).toBe(true);
    expect(showsProcedureLink({ idSubmittedDocument: 1, procedureId: undefined })).toBe(true);
    expect(showsProcedureLink({ idSubmittedDocument: 1, procedureId: 11 })).toBe(false);
  });

  it("maps a complete form to the OpenAPI create request (#1260)", () => {
    expect(toDocumentRequest(complete)).toEqual({
      typeId: 3,
      date: "2026-09-05",
      delivered: false,
      procedureId: 11,
    });
    expect(toDocumentRequest({ ...complete, fecha: "" })).toEqual({
      typeId: 3,
      date: undefined,
      delivered: false,
      procedureId: 11,
    });
  });
});
