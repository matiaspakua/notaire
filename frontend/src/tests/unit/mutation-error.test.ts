import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "@/lib/api-client";
import {
  parseFieldErrors,
  presentMutationError,
} from "@/lib/mutation-error";

vi.mock("sonner", () => ({
  toast: { error: vi.fn(), success: vi.fn() },
}));

describe("parseFieldErrors()", () => {
  it("parses bean-validation joined field: message pairs", () => {
    expect(
      parseFieldErrors("name: must not be blank; amount: must be positive", [
        "name",
        "amount",
      ])
    ).toEqual({
      name: "must not be blank",
      amount: "must be positive",
    });
  });

  it("ignores segments that do not match known form fields", () => {
    expect(
      parseFieldErrors("unknownField: bad; name: required", ["name"])
    ).toEqual({ name: "required" });
  });

  it("returns empty map when message has no field detail", () => {
    expect(parseFieldErrors("Saldo excedido", ["amount"])).toEqual({});
  });
});

describe("presentMutationError()", () => {
  beforeEach(async () => {
    const { toast } = await import("sonner");
    vi.mocked(toast.error).mockClear();
  });

  it("toasts the server message from an ApiError body (not only the fallback)", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(
      409,
      "/pagos",
      JSON.stringify({ message: "El monto excede el saldo pendiente" })
    );

    const result = presentMutationError(err, { fallback: "Error al guardar" });

    expect(result.message).toBe("El monto excede el saldo pendiente");
    expect(toast.error).toHaveBeenCalledWith("El monto excede el saldo pendiente");
    expect(result.toasted).toBe(true);
  });

  it("falls back when the body has no parseable business message", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(500, "/pagos", "plain text");

    const result = presentMutationError(err, { fallback: "Error al guardar" });

    expect(result.message).toBe("Error al guardar");
    expect(toast.error).toHaveBeenCalledWith("Error al guardar");
  });

  it("does not toast authenticated 401 (session-expiry owns that path)", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(401, "/usuarios", "Unauthorized");

    const result = presentMutationError(err, { fallback: "Error al guardar" });

    expect(result.toasted).toBe(false);
    expect(toast.error).not.toHaveBeenCalled();
  });

  it("maps field detail into fieldErrors and invokes setFieldErrors", async () => {
    const { toast } = await import("sonner");
    const setFieldErrors = vi.fn();
    const err = new ApiError(
      400,
      "/roles",
      JSON.stringify({ message: "name: must not be blank" })
    );

    const result = presentMutationError(err, {
      fallback: "Error al guardar el rol",
      fieldNames: ["name"],
      setFieldErrors,
    });

    expect(result.fieldErrors).toEqual({ name: "must not be blank" });
    expect(setFieldErrors).toHaveBeenCalledWith({ name: "must not be blank" });
    // Unmapped-only path still toasts; mapped fields also keep the full server toast.
    expect(toast.error).toHaveBeenCalledWith("name: must not be blank");
  });

  it("toasts the full message when field-like detail does not match known controls", async () => {
    const { toast } = await import("sonner");
    const setFieldErrors = vi.fn();
    const err = new ApiError(
      400,
      "/roles",
      JSON.stringify({ message: "other: invalid value" })
    );

    const result = presentMutationError(err, {
      fallback: "Error al guardar el rol",
      fieldNames: ["name"],
      setFieldErrors,
    });

    expect(result.fieldErrors).toEqual({});
    expect(setFieldErrors).not.toHaveBeenCalled();
    expect(toast.error).toHaveBeenCalledWith("other: invalid value");
  });

  it("extracts messages for 404 ApiError bodies", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(
      404,
      "/documentos/99",
      JSON.stringify({ message: "Documento no encontrado" })
    );

    presentMutationError(err, { fallback: "Error al eliminar" });
    expect(toast.error).toHaveBeenCalledWith("Documento no encontrado");
  });

  it("preferFallback toasts curated fallback even when body has a message", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(
      400,
      "/presupuestos/1/items-desde-plantilla",
      JSON.stringify({ message: "El tipo de trámite no tiene plantilla configurada" })
    );

    const result = presentMutationError(err, {
      fallback: "El tipo de trámite seleccionado no tiene una plantilla configurada",
      preferFallback: true,
    });

    expect(result.message).toBe(
      "El tipo de trámite seleccionado no tiene una plantilla configurada"
    );
    expect(toast.error).toHaveBeenCalledWith(
      "El tipo de trámite seleccionado no tiene una plantilla configurada"
    );
  });
});
