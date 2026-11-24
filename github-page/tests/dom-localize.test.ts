import { describe, expect, it } from "vitest";

import { ES } from "@/lib/i18n/es";
import { localizeTree, observeLocalized } from "@/lib/i18n/dom";

ES["Review and merge"] = "Review y merge";
ES["Role"] = "Rol";
ES["Who"] = "Quién";

function root(html: string): HTMLElement {
  const el = document.createElement("div");
  el.innerHTML = html;
  document.body.appendChild(el);
  return el;
}

/** #2475: tables' aria-labels stayed English, and content rendered after mount (Mermaid, figures) was never swapped. */
describe("DOM localisation", () => {
  it("translates text nodes and restores English", () => {
    const el = root("<p>Review and merge</p>");
    localizeTree(el, "es");
    expect(el.textContent).toBe("Review y merge");
    localizeTree(el, "en");
    expect(el.textContent).toBe("Review and merge");
  });

  it("translates accessibility attributes, including the Table: prefix", () => {
    const el = root('<div aria-label="Table: Role, Who"></div><button title="Review and merge"></button>');
    localizeTree(el, "es");
    expect(el.querySelector("div")?.getAttribute("aria-label")).toBe("Tabla: Rol, Quién");
    expect(el.querySelector("button")?.getAttribute("title")).toBe("Review y merge");
    localizeTree(el, "en");
    expect(el.querySelector("div")?.getAttribute("aria-label")).toBe("Table: Role, Who");
  });

  it("keeps translating content added after the first pass", async () => {
    const el = root("<p>Role</p>");
    const stop = observeLocalized(el, "es");
    const late = document.createElement("span");
    late.textContent = "Review and merge";
    el.appendChild(late);
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(late.textContent).toBe("Review y merge");
    stop();
  });
});
