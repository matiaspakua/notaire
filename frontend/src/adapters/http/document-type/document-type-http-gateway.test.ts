// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the document-type domain (#1777).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { documentTypeHttpGateway } from "./document-type-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("documentTypeHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists and maps idDocumentType to domain id", async () => {
    apiGetMock.mockResolvedValue([
      { idDocumentType: 1, name: "ID card", enabled: true, returned: false },
    ]);
    await expect(documentTypeHttpGateway.list()).resolves.toEqual([
      {
        id: 1,
        name: "ID card",
        expires: undefined,
        dueDays: undefined,
        deliveredBy: undefined,
        enabled: true,
        returned: false,
      },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/document-types");
  });

  it("searches, creates, updates, deletes and checks in-use", async () => {
    apiGetMock.mockResolvedValueOnce([{ idDocumentType: 2, name: "Deed", expires: true, dueDays: 30 }]);
    await expect(documentTypeHttpGateway.searchByName("Deed")).resolves.toMatchObject([
      { id: 2, name: "Deed", expires: true, dueDays: 30 },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/document-types/search?name=Deed");

    apiPostMock.mockResolvedValue({
      idDocumentType: 9,
      name: "Passport",
      enabled: true,
    });
    await expect(
      documentTypeHttpGateway.create({ name: "Passport", enabled: true }),
    ).resolves.toMatchObject({ id: 9, name: "Passport" });

    apiPutMock.mockResolvedValue({
      idDocumentType: 9,
      name: "Passport copy",
      enabled: true,
      expires: true,
      dueDays: 7,
    });
    await expect(
      documentTypeHttpGateway.update(9, {
        name: "Passport copy",
        enabled: true,
        expires: true,
        dueDays: 7,
      }),
    ).resolves.toMatchObject({ id: 9, name: "Passport copy", dueDays: 7 });

    apiDeleteMock.mockResolvedValue(undefined);
    await documentTypeHttpGateway.delete(9);
    expect(apiDeleteMock).toHaveBeenCalledWith("/document-types/9");

    apiGetMock.mockResolvedValueOnce({ inUse: true });
    await expect(documentTypeHttpGateway.isInUse(1)).resolves.toBe(true);
    expect(apiGetMock).toHaveBeenCalledWith("/document-types/1/in-use");
  });
});
