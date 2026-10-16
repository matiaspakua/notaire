// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the person domain (#1823).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiGetPage: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiGetPage, apiPost, apiPut } from "@/lib/api-client";
import { personHttpGateway } from "./person-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiGetPageMock = vi.mocked(apiGetPage);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("personHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("gets by id and maps personId to domain id", async () => {
    apiGetMock.mockResolvedValue({ personId: 7, firstName: "Carla", lastName: "Garcia", version: 0 });
    await expect(personHttpGateway.getById(7)).resolves.toEqual({
      id: 7,
      firstName: "Carla",
      lastName: "Garcia",
      nationality: undefined,
      identificationNumber: undefined,
      taxId: undefined,
      sex: undefined,
      birthDate: undefined,
      maritalStatus: undefined,
      marriageCount: undefined,
      occupation: undefined,
      address: undefined,
      phone: undefined,
      email: undefined,
      notaryRegistrationNumber: undefined,
      isClient: undefined,
      identificationTypeId: null,
      identificationTypeName: undefined,
      identificationTypeCharacters: undefined,
      version: 0,
    });
    expect(apiGetMock).toHaveBeenCalledWith("/people/7");
  });

  it("lists a page, searches, creates, updates and deletes", async () => {
    apiGetPageMock.mockResolvedValue({
      content: [{ personId: 1, firstName: "Ana", lastName: "Latest" }],
      number: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    });
    await expect(personHttpGateway.listPage({ page: 0, size: 20, sort: "idPerson,desc" })).resolves.toMatchObject({
      content: [{ id: 1, firstName: "Ana" }],
      totalElements: 1,
    });
    expect(apiGetPageMock).toHaveBeenCalledWith("/people", {
      page: 0,
      size: 20,
      sort: "idPerson,desc",
    });

    apiGetMock.mockResolvedValueOnce([{ personId: 2, lastName: "Perez" }]);
    await expect(personHttpGateway.search({ lastName: "Perez" })).resolves.toMatchObject([
      { id: 2, lastName: "Perez" },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/people/search?lastName=Perez");

    apiPostMock.mockResolvedValue(undefined);
    await personHttpGateway.create({ firstName: "New", lastName: "Person" });
    expect(apiPostMock).toHaveBeenCalledWith("/people", expect.objectContaining({ firstName: "New" }));

    apiPutMock.mockResolvedValue(undefined);
    await personHttpGateway.update(2, { firstName: "Updated", lastName: "Perez" });
    expect(apiPutMock).toHaveBeenCalledWith("/people/2", expect.objectContaining({ firstName: "Updated" }));

    apiDeleteMock.mockResolvedValue(undefined);
    await personHttpGateway.delete(2);
    expect(apiDeleteMock).toHaveBeenCalledWith("/people/2");
  });
});
