/**
 * Dialogs have an accessible name and a localized close button (issue #1344).
 * The visible form heading is the Radix DialogTitle, so `role=dialog` is
 * labelled by text the user sees and Radix logs no DialogTitle/Description
 * warning.
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { NextIntlClientProvider } from "next-intl";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormHeader, FormSection } from "@/theme/form-patterns";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";

function renderDialog(node: React.ReactNode, locale: "es" | "en" = "es") {
  return render(
    <NextIntlClientProvider locale={locale} messages={locale === "es" ? es : en}>
      <Dialog open onOpenChange={() => {}}>
        <DialogContent>{node}</DialogContent>
      </Dialog>
    </NextIntlClientProvider>
  );
}

describe("DialogContent accessible name (#1344)", () => {
  let errorSpy: ReturnType<typeof vi.spyOn>;
  let warnSpy: ReturnType<typeof vi.spyOn>;
  beforeEach(() => {
    errorSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
  });
  afterEach(() => {
    errorSpy.mockRestore();
    warnSpy.mockRestore();
  });

  it("a FormSection marked dialogTitle names the dialog with its visible heading", () => {
    renderDialog(<FormSection dialogTitle title="Editar presupuesto"><input aria-label="x" /></FormSection>);
    const dialog = screen.getByRole("dialog", { name: "Editar presupuesto" });
    const labelId = dialog.getAttribute("aria-labelledby");
    expect(labelId).toBeTruthy();
    expect(document.getElementById(labelId!)).toHaveTextContent("Editar presupuesto");
    expect(document.getElementById(labelId!)?.tagName).toBe("H3");
  });

  it("a FormHeader marked dialogTitle names the dialog", () => {
    renderDialog(<FormHeader dialogTitle title="Resumen del presupuesto" />);
    expect(screen.getByRole("dialog", { name: "Resumen del presupuesto" })).toBeInTheDocument();
  });

  it("a plain FormSection is not a dialog title (only one title per dialog)", () => {
    renderDialog(
      <>
        <FormHeader dialogTitle title="Ítems" />
        <FormSection title="Plantilla"><span /></FormSection>
      </>
    );
    expect(screen.getByRole("dialog", { name: "Ítems" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Plantilla" }).id).toBe("");
  });

  it("logs no Radix DialogTitle or aria-describedby warning", () => {
    renderDialog(<FormSection dialogTitle title="Nueva persona"><span /></FormSection>);
    const logged = [...errorSpy.mock.calls, ...warnSpy.mock.calls].map((c) => String(c[0])).join("\n");
    expect(logged).not.toMatch(/DialogTitle|Description|aria-describedby/);
    expect(screen.getByRole("dialog")).not.toHaveAttribute("aria-describedby");
  });

  it("the close button is named in the active locale", () => {
    const { unmount } = renderDialog(<FormSection dialogTitle title="Nueva persona"><span /></FormSection>, "es");
    expect(screen.getByRole("button", { name: "Cerrar" })).toBeInTheDocument();
    unmount();
    renderDialog(<FormSection dialogTitle title="New person"><span /></FormSection>, "en");
    expect(screen.getByRole("button", { name: "Close" })).toBeInTheDocument();
  });
});

describe("common.close i18n", () => {
  it("is defined in es and en", () => {
    expect((es.common as Record<string, string>).close).toBe("Cerrar");
    expect((en.common as Record<string, string>).close).toBe("Close");
  });
});

function tsxFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const p = join(dir, name);
    return statSync(p).isDirectory() ? tsxFiles(p) : p.endsWith(".tsx") ? [p] : [];
  });
}

describe("every DialogContent has a title (static scan of src/app)", () => {
  it("names each dialog with a dialogTitle heading or a DialogTitle", () => {
    const missing: string[] = [];
    for (const file of tsxFiles(join(__dirname, "../../app"))) {
      const src = readFileSync(file, "utf8");
      let from = 0;
      for (;;) {
        const start = src.indexOf("<DialogContent", from);
        if (start < 0) break;
        const end = src.indexOf("</DialogContent>", start);
        const body = src.slice(start, end < 0 ? undefined : end);
        if (!/<(FormSection|FormHeader)\b[^>]*\bdialogTitle\b|<DialogTitle\b/.test(body)) {
          missing.push(`${file.split("/src/")[1]}:${src.slice(0, start).split("\n").length}`);
        }
        from = start + 1;
      }
    }
    expect(missing).toEqual([]);
  });
});
