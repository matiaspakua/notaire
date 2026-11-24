import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { ModelView } from "@/components/docs/ModelView";
import {
  MODEL_FINDINGS,
  MODEL_GROUPS,
  MODEL_SITE_HREF,
  MODEL_VIEWS,
  modelView,
  modelViewHref,
} from "@/lib/architecture-model";
import { pageIsTranslated } from "@/lib/i18n";
import { ES } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { DOCS_PAGES } from "@/lib/site";

/** #2483: the Pages architecture views come from the LikeC4 model, not from hand-written charts. */
describe("LikeC4 architecture model on Pages", () => {
  it("publishes every view family the docs promise", () => {
    const ids = MODEL_VIEWS.map((v) => v.id);
    for (const id of ["index", "containers", "backend", "frontend", "security", "securityPipeline", "loginMfa",
      "apiRequest", "testing", "delivery", "dev", "prod", "staging", "findings"]) {
      expect(ids, id).toContain(id);
    }
  });

  it("groups every view exactly once", () => {
    const grouped = MODEL_GROUPS.flatMap((g) => g.views);
    expect([...grouped].sort()).toEqual(MODEL_VIEWS.map((v) => v.id).sort());
  });

  it("links each view into the interactive site with hash routing", () => {
    expect(MODEL_SITE_HREF).toBe("/architecture-model/");
    expect(modelViewHref("containers")).toBe("/architecture-model/#/view/containers/");
  });

  it("keeps the charts parseable: no statement-breaking semicolons", () => {
    for (const view of MODEL_VIEWS) {
      expect(view.mermaid, view.id).not.toContain(";");
      expect(view.mermaid, view.id).toMatch(/^---\ntitle: /);
    }
  });

  it("lists the findings with their Issue", () => {
    expect(MODEL_FINDINGS.length).toBeGreaterThanOrEqual(5);
    for (const f of MODEL_FINDINGS) expect(f.issue).toMatch(/\/issues\/\d+$/);
  });

  it("renders a view with its title, description and an interactive link", () => {
    render(<ModelView id="containers" />);
    const view = modelView("containers");
    expect(screen.getByRole("heading", { name: view.title })).toBeInTheDocument();
    expect(screen.getByText(view.description)).toBeInTheDocument();
    expect(screen.getByRole("img", { name: view.title })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /open .* interactive model/i })).toHaveAttribute(
      "href",
      "/architecture-model/#/view/containers/",
    );
  });

  it("has a docs page, translated, with Spanish for every view title and description", () => {
    expect(DOCS_PAGES.map((p) => p.href)).toContain("/docs/architecture/model/");
    expect(pageIsTranslated("/docs/architecture/model/")).toBe(true);
    for (const view of MODEL_VIEWS) {
      expect(ES[view.title], view.title).toBeTruthy();
      expect(ES[view.description], view.description).toBeTruthy();
    }
    for (const group of MODEL_GROUPS) expect(ES[group.label], group.label).toBeTruthy();
  });
});
