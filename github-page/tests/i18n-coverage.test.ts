import { describe, expect, it } from "vitest";
import {
  CASE_CHAPTERS,
  CASE_LESSONS,
  FRAMEWORK_COMMANDS,
  FRAMEWORK_LEVELS,
  FRAMEWORK_PRINCIPLES,
  FRAMEWORK_STAGES,
  LESSONS,
  PLAYBOOK,
  PRODUCTION_READY,
} from "@/lib/ai-sdlc-content";
import { pageIsTranslated } from "@/lib/i18n";
import { ES, TRANSLATED_ROUTES } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { LINDDUN, POSTURE, STRIDE } from "@/lib/security-content";
import { DOCS_PAGES, FACTORY_PAGES, PRODUCT_PAGES, SECURITY_PAGES, OTHER_PAGES } from "@/lib/site";

function need(label: string, value: string) {
  const es = ES[value];
  expect(es, `${label}: ${value.slice(0, 80)}`).toBeTruthy();
  if (value.length > 24 && !value.includes("validate-sdlc-plan") && !value.endsWith(".sh") && !value.endsWith(".yml")) {
    expect(es, `${label} still English`).not.toBe(value);
  }
}

describe("Spanish keys for translated routes (#2202)", () => {
  it("covers every route this change promises", () => {
    for (const href of [...FACTORY_PAGES, ...PRODUCT_PAGES, ...SECURITY_PAGES, ...OTHER_PAGES].map((p) => p.href)) {
      expect(pageIsTranslated(href), href).toBe(true);
    }
    for (const href of ["/docs/", "/docs/artifacts/", "/docs/architecture/", "/docs/engineering/", "/docs/testing/", "/docs/releases/"]) {
      expect(pageIsTranslated(href), href).toBe(true);
    }
    expect(pageIsTranslated("/docs/hexagonal/")).toBe(false);
    expect(pageIsTranslated("/docs/glossary/")).toBe(false);
    expect(ES["Case management for a notary office"]).toMatch(/escribanía/);
    expect(ES["What the factory produced"]).toMatch(/fábrica/);
    expect(ES["Notaire knowledge base"]).toMatch(/conocimiento/);
    expect(TRANSLATED_ROUTES).toContain("/about/");
  });

  it("has Spanish for lessons, the playbook, the case, the kit tables and the threat cards", () => {
    for (const item of LESSONS) {
      need(item.title, item.title);
      need(item.title, item.happened);
      need(item.title, item.rule);
    }
    for (const step of PLAYBOOK) {
      need(step.title, step.title);
      need(step.title, step.body);
    }
    for (const chapter of CASE_CHAPTERS) {
      need(chapter.id, chapter.title);
      need(chapter.id, chapter.problem);
      need(chapter.id, chapter.move);
      need(chapter.id, chapter.result);
    }
    for (const item of CASE_LESSONS) {
      need(item.title, item.title);
      need(item.title, item.body);
      need(item.title, item.tradeoff);
    }
    for (const item of FRAMEWORK_PRINCIPLES) {
      need(item.k, item.k);
      need(item.k, item.v);
    }
    for (const row of FRAMEWORK_STAGES) {
      need(row.stage, row.done);
      need(row.stage, row.notaire);
    }
    for (const row of FRAMEWORK_LEVELS) {
      need(row.name, row.name);
      need(row.name, row.fits);
      need(row.name, row.adds);
    }
    for (const row of FRAMEWORK_COMMANDS) need(row.cmd, row.does);
    for (const block of STRIDE) {
      need(block.letter, block.question);
      for (const item of block.items) need(item.id, item.summary);
    }
    for (const row of LINDDUN) {
      need(row.id, row.name);
      need(row.id, row.summary);
    }
    for (const row of POSTURE) {
      need(row.id, row.title);
      need(row.id, row.summary);
    }
    expect(DOCS_PAGES.find((p) => p.href === "/docs/hexagonal/")).toBeTruthy();
  });
});
