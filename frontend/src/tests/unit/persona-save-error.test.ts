import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "@/lib/api-client";
import { presentPersonaSaveError } from "@/lib/persona-save-error";
import type { Persona } from "@/types";

vi.mock("sonner", () => ({
  toast: { error: vi.fn(), success: vi.fn() },
}));

describe("presentPersonaSaveError()", () => {
  const setFieldErrors = vi.fn();
  const onViewExisting = vi.fn();
  const personas: Persona[] = [
    {
      personId: 42,
      firstName: "Ana",
      lastName: "Existente",
      identificationNumber: "123",
      isClient: false,
    },
  ];

  beforeEach(async () => {
    const { toast } = await import("sonner");
    vi.mocked(toast.error).mockClear();
    setFieldErrors.mockClear();
    onViewExisting.mockClear();
  });

  it("toasts backend 400 validation message and maps identificationNumber field error", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(
      400,
      "/people",
      JSON.stringify({ message: "identificationNumber: must not be blank" })
    );

    const result = presentPersonaSaveError(err, {
      fallback: "Error al guardar la persona",
      duplicateDocument: "Ya existe otra persona registrada con este documento.",
      viewExistingLabel: "Ver persona existente",
      personas,
      onViewExisting,
      setFieldErrors,
    });

    expect(result.message).toBe("identificationNumber: must not be blank");
    expect(result.fieldErrors).toEqual({
      identificationNumber: "must not be blank",
    });
    expect(setFieldErrors).toHaveBeenCalledWith({
      identificationNumber: "must not be blank",
    });
    expect(toast.error).toHaveBeenCalledWith(
      "identificationNumber: must not be blank"
    );
    expect(result.usedDuplicatePath).toBe(false);
  });

  it("falls back to generic save error when no message is extractable", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(500, "/people", "plain text");

    const result = presentPersonaSaveError(err, {
      fallback: "Error al guardar la persona",
      duplicateDocument: "Ya existe otra persona registrada con este documento.",
      viewExistingLabel: "Ver persona existente",
      personas,
      onViewExisting,
      setFieldErrors,
    });

    expect(result.message).toBe("Error al guardar la persona");
    expect(toast.error).toHaveBeenCalledWith("Error al guardar la persona");
    expect(result.usedDuplicatePath).toBe(false);
  });

  it("keeps curated localized 409 duplicate toast (not raw English API text)", async () => {
    const { toast } = await import("sonner");
    const err = new ApiError(
      409,
      "/people",
      JSON.stringify({
        message: "Duplicate identification number",
        existingPersonId: 42,
      })
    );

    const result = presentPersonaSaveError(err, {
      fallback: "Error al guardar la persona",
      duplicateDocument: "Ya existe otra persona registrada con este documento.",
      viewExistingLabel: "Ver persona existente",
      personas,
      onViewExisting,
      setFieldErrors,
    });

    expect(result.usedDuplicatePath).toBe(true);
    expect(toast.error).toHaveBeenCalledWith(
      "Ya existe otra persona registrada con este documento.",
      expect.objectContaining({
        action: expect.objectContaining({ label: "Ver persona existente" }),
      })
    );
    const call = vi.mocked(toast.error).mock.calls[0];
    const opts = call[1] as { action?: { onClick: () => void } };
    opts.action?.onClick();
    expect(onViewExisting).toHaveBeenCalledWith(personas[0]);
    expect(setFieldErrors).not.toHaveBeenCalled();
  });
});
