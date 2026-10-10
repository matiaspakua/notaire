/**
 * Delete failures keep the backend's reason (issue #1345).
 * A 409 means the record is still referenced: it is shown as a warning with
 * the translated "in use" text and the server detail, never as the generic
 * "Error al eliminar".
 */
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "@/lib/api-client";
import { presentDeleteError } from "@/lib/mutation-error";
import es from "../../../messages/es.json";
import en from "../../../messages/en.json";

vi.mock("sonner", () => ({
  toast: { error: vi.fn(), warning: vi.fn(), success: vi.fn() },
}));

const OPTS = {
  fallback: "Error al eliminar",
  inUse: "No se puede eliminar: el registro está en uso.",
  notFound: "El registro ya no existe.",
};

describe("presentDeleteError()", () => {
  beforeEach(async () => {
    const { toast } = await import("sonner");
    vi.mocked(toast.error).mockClear();
    vi.mocked(toast.warning).mockClear();
  });

  it("409 with a plain-text body warns with the in-use text and keeps the server reason", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(409, "/people/1", "Cannot delete: person is referenced by other records.");

    const result = presentDeleteError(err, OPTS);

    expect(result.kind).toBe("in-use");
    expect(toast.warning).toHaveBeenCalledWith(OPTS.inUse, {
      description: "Cannot delete: person is referenced by other records.",
    });
    expect(toast.error).not.toHaveBeenCalled();
  });

  it("409 with a JSON body uses its message as the detail", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(409, "/conceptos/3", JSON.stringify({ message: "Concepto usado en 2 presupuestos" }));

    presentDeleteError(err, OPTS);

    expect(toast.warning).toHaveBeenCalledWith(OPTS.inUse, { description: "Concepto usado en 2 presupuestos" });
  });

  it("409 without a body still warns with the in-use text", async () => {
    const { toast } = await import("sonner");
    presentDeleteError(new ApiError(409, "/gestiones/9", ""), OPTS);
    expect(toast.warning).toHaveBeenCalledWith(OPTS.inUse, undefined);
  });

  it("404 says the record no longer exists", async () => {
    const { toast } = await import("sonner");
    const result = presentDeleteError(new ApiError(404, "/escrituras/9", ""), OPTS);
    expect(result.kind).toBe("not-found");
    expect(toast.error).toHaveBeenCalledWith(OPTS.notFound);
  });

  it("500 with a JSON message shows it; without one shows the fallback", async () => {
    const { toast } = await import("sonner");
    presentDeleteError(new ApiError(500, "/x/1", JSON.stringify({ message: "Unexpected error" })), OPTS);
    expect(toast.error).toHaveBeenLastCalledWith("Unexpected error");
    presentDeleteError(new ApiError(500, "/x/1", "<html>oops</html>"), OPTS);
    expect(toast.error).toHaveBeenLastCalledWith(OPTS.fallback);
  });

  it("a network error shows the fallback", async () => {
    const { toast } = await import("sonner");
    const result = presentDeleteError(new TypeError("Failed to fetch"), OPTS);
    expect(result.kind).toBe("error");
    expect(toast.error).toHaveBeenCalledWith(OPTS.fallback);
  });

  it("401 is left to the session-expiry handler", async () => {
    const { toast } = await import("sonner");
    const result = presentDeleteError(new ApiError(401, "/x/1", ""), OPTS);
    expect(result.kind).toBe("ignored");
    expect(toast.error).not.toHaveBeenCalled();
    expect(toast.warning).not.toHaveBeenCalled();
  });
});

describe("delete error i18n", () => {
  it("defines common.errors.inUse and common.errors.notFound in es and en", () => {
    for (const m of [es, en] as Array<{ common: { errors?: Record<string, string> } }>) {
      expect(m.common.errors?.inUse).toBeTruthy();
      expect(m.common.errors?.notFound).toBeTruthy();
    }
    expect(es.common.errors.inUse).not.toBe(en.common.errors.inUse);
  });
});

function pageFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const p = join(dir, name);
    return statSync(p).isDirectory() ? pageFiles(p) : p.endsWith(".tsx") ? [p] : [];
  });
}

describe("delete handlers in src/app (static scan)", () => {
  const files = pageFiles(join(__dirname, "../../app"));

  it("never toast the generic delete error directly", () => {
    const offenders = files.filter((f) => /toast\.error\([^;]*errorDelete/.test(readFileSync(f, "utf8")));
    expect(offenders).toEqual([]);
  });

  it("route delete failures through presentDeleteError", () => {
    const offenders = files.filter((f) =>
      /presentMutationError\(\s*err,\s*\{\s*fallback:\s*t\("[\w.]*errorDelete"/.test(readFileSync(f, "utf8"))
    );
    expect(offenders).toEqual([]);
  });
});
