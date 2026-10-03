/**
 * #800 — residual DocumentType enabled/returned on admin form (CU27/CU32).
 * TDD gate: EMPTY defaults + type/DTO field presence + form wiring snippets.
 */
import { readFileSync } from "node:fs";
import { join } from "node:path";
import { describe, it, expect } from "vitest";
import { EMPTY_DOCUMENT_TYPE } from "@/lib/document-type-form";
import type { TipoDeDocumento } from "@/types";

const ROOT = join(__dirname, "../../..");

describe("Document type enabled/returned form defaults (#800)", () => {
  it("should default enabled true and returned false on EMPTY create", () => {
    expect(EMPTY_DOCUMENT_TYPE.enabled).toBe(true);
    expect(EMPTY_DOCUMENT_TYPE.returned).toBe(false);
    expect(EMPTY_DOCUMENT_TYPE.expires).toBe(false);
  });

  it("should include returned on the TipoDeDocumento type surface", () => {
    const sample: TipoDeDocumento = {
      name: "Deed",
      enabled: true,
      returned: true,
    };
    expect(sample.returned).toBe(true);
    expect(sample.enabled).toBe(true);
  });

  it("should wire enabled and returned CheckboxFields on the admin page", () => {
    const src = readFileSync(
      join(ROOT, "src/app/dashboard/administracion/documentos/page.tsx"),
      "utf8",
    );
    expect(src).toContain('data-testid="checkbox-enabled-documento"');
    expect(src).toContain('data-testid="checkbox-returned-documento"');
    expect(src).toContain("EMPTY_DOCUMENT_TYPE");
    expect(src).toContain("enabled:");
    expect(src).toContain("returned:");
  });

  it("should declare i18n keys for enabled and returned fields", () => {
    const en = JSON.parse(readFileSync(join(ROOT, "messages/en.json"), "utf8"));
    const es = JSON.parse(readFileSync(join(ROOT, "messages/es.json"), "utf8"));
    expect(en.administracion.documentos.fields.enabled).toBeTruthy();
    expect(en.administracion.documentos.fields.returned).toBeTruthy();
    expect(es.administracion.documentos.fields.enabled).toBeTruthy();
    expect(es.administracion.documentos.fields.returned).toBeTruthy();
  });
});
