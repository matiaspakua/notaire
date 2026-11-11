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
import { engineeringFigure } from "@/components/anatomy/engineering-figure";
import { sdlcFigure } from "@/components/anatomy/sdlc-figure";
import { systemFigure } from "@/components/anatomy/system-figure";
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
    for (const href of DOCS_PAGES.map((p) => p.href)) {
      expect(pageIsTranslated(href), href).toBe(true);
    }
    expect(ES["Case management for a notary office"]).toMatch(/escribanía/);
    expect(ES["What the factory produced"]).toMatch(/fábrica/);
    expect(ES["Notaire knowledge base"]).toMatch(/conocimiento/);
    expect(ES["Every term, in plain words"]).toMatch(/término/);
    expect(ES["Roadmap and progress"]).toMatch(/avance/);
    expect(ES["Module ownership"]).toMatch(/módulo/);
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

  it("has Spanish for anatomy figure plate copy and steps", () => {
    need("chrome", "Where to look:");
    need("chrome", "AI Factory chapters");
    for (const fig of [systemFigure, engineeringFigure, sdlcFigure]) {
      need(fig.id, fig.title);
      need(fig.id, fig.hint);
      need(fig.id, fig.caption);
      for (const key of fig.keys) need(fig.id, key.label);
      for (const step of fig.steps) {
        need(`${fig.id}/${step.station}`, step.title);
        need(`${fig.id}/${step.station}`, step.text);
        need(`${fig.id}/${step.station}`, step.readout);
      }
    }
    expect(ES["The notary's counter"]).toMatch(/escribanía|mostrador/i);
    expect(ES["The lock chamber"]).toMatch(/esclusa|cámara/i);
    expect(ES["The release line"]).toMatch(/línea|release/i);
  });

  it("has Spanish for key Mermaid sentence labels and diagram captions", () => {
    need("mermaid", "On the agent's machine — the pull-request proof");
    need("mermaid", "2 · Refine requirement");
    need(
      "caption",
      "CONSTITUTION.md §5 Official SDLC Workflow. The yellow hexagons are the hard gates of §6.",
    );
    need(
      "caption",
      "Purple: policy and context. Blue: mechanical enforcement. Red: the human Owner.",
    );
    expect(ES["On the agent's machine — the pull-request proof"]).not.toBe(
      "On the agent's machine — the pull-request proof",
    );
  });
});
