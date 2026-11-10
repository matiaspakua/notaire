import { describe, expect, it } from "vitest";
import { chrome, homeCopy } from "@/lib/i18n";

describe("Spanish copy (#2201)", () => {
  it("names technical docs in Spanish and keeps the English label", () => {
    expect(chrome("en").nav.docs).toBe("Technical docs");
    expect(chrome("es").nav.docs).toBe("Documentación técnica");
    expect(chrome("es").nav.factory).toBe("AI Factory");
    expect(chrome("en").search).toMatch(/search/i);
    expect(chrome("es").search).toMatch(/buscar/i);
  });

  it("tells the home story in Spanish without dropping the English terms", () => {
    const es = homeCopy("es");
    const en = homeCopy("en");
    expect(es.titleEm.toLowerCase()).toMatch(/paso/);
    expect(en.titleEm.toLowerCase()).toMatch(/step/);
    expect(es.lead).toMatch(/OpenSpec|gate|Issue|pull request|Flyway|commit/);
    expect(es.lead).not.toMatch(/\bvos\b/);
    expect(es.proof.openspec.length).toBeGreaterThan(8);
    expect(es.ctaDemo).toMatch(/demo/i);
  });
});
