// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the identification-type domain (#1732).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { identificationTypeHttpGateway } from "./identification-type-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("identificationTypeHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists and maps idIdentificationType to domain id", async () => {
    apiGetMock.mockResolvedValue([
      { idIdentificationType: 1, name: "DNI", characters: "8", version: 0 },
    ]);
    await expect(identificationTypeHttpGateway.list()).resolves.toEqual([
      { id: 1, name: "DNI", characters: "8", version: 0 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/identification-types");
  });

  it("creates, updates and deletes via the REST paths", async () => {
    apiPostMock.mockResolvedValue({
      idIdentificationType: 9,
      name: "Pasaporte",
      characters: "12",
      version: 0,
    });
    await expect(
      identificationTypeHttpGateway.create({ name: "Pasaporte", characters: "12" }),
    ).resolves.toMatchObject({ id: 9, name: "Pasaporte" });
    expect(apiPostMock).toHaveBeenCalledWith("/identification-types", {
      name: "Pasaporte",
      characters: "12",
    });

    apiPutMock.mockResolvedValue({
      idIdentificationType: 9,
      name: "Passport",
      characters: "12",
      version: 1,
    });
    await expect(
      identificationTypeHttpGateway.update(9, { name: "Passport", characters: "12" }),
    ).resolves.toMatchObject({ id: 9, name: "Passport", version: 1 });
    expect(apiPutMock).toHaveBeenCalledWith("/identification-types/9", {
      name: "Passport",
      characters: "12",
    });

    apiDeleteMock.mockResolvedValue(undefined);
    await identificationTypeHttpGateway.delete(9);
    expect(apiDeleteMock).toHaveBeenCalledWith("/identification-types/9");
  });
});
