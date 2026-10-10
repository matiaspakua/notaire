import { describe, it, expect } from "vitest";
import {
  EMPTY_DOCUMENTO_FORM,
  missingDocumentoFields,
  showsTramiteLink,
  toDocumentoRequest,
} from "@/lib/documento-presentado-form";

/**
 * CU04 / CU72 (#655): a submitted document is presented for a trámite of a
 * gestión, so the create form requires the document type and the trámite.
 */
describe("documento presentado form (#655)", () => {
  const complete = { ...EMPTY_DOCUMENTO_FORM, tipoId: "3", fecha: "2026-09-05", gestionId: "7", tramiteId: "11" };

  it("requires the type and the trámite on create", () => {
    expect(missingDocumentoFields(EMPTY_DOCUMENTO_FORM, false)).toEqual(["tipoId", "tramiteId"]);
    expect(missingDocumentoFields({ ...complete, tramiteId: "" }, false)).toEqual(["tramiteId"]);
    expect(missingDocumentoFields({ ...complete, tipoId: "" }, false)).toEqual(["tipoId"]);
    expect(missingDocumentoFields(complete, false)).toEqual([]);
  });

  it("does not force a trámite when editing a legacy document without one", () => {
    expect(missingDocumentoFields({ ...complete, gestionId: "", tramiteId: "" }, true)).toEqual([]);
  });

  it("offers the gestión/trámite link on create and on documents that have none yet", () => {
    expect(showsTramiteLink(null)).toBe(true);
    expect(showsTramiteLink({ idSubmittedDocument: 1, procedureId: undefined })).toBe(true);
    expect(showsTramiteLink({ idSubmittedDocument: 1, procedureId: 11 })).toBe(false);
  });

  it("maps a complete form to the OpenAPI create request (#1260)", () => {
    expect(toDocumentoRequest(complete)).toEqual({
      typeId: 3,
      date: "2026-09-05",
      delivered: false,
      procedureId: 11,
    });
    expect(toDocumentoRequest({ ...complete, fecha: "" })).toEqual({
      typeId: 3,
      date: undefined,
      delivered: false,
      procedureId: 11,
    });
  });
});
