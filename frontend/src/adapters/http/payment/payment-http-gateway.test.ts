// @vitest-environment node
/**
 * HTTP gateway maps wire DTOs to the payment domain (#1832).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiGet: vi.fn(),
  apiPost: vi.fn(),
  apiPut: vi.fn(),
  apiDelete: vi.fn(),
}));

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api-client";
import { paymentHttpGateway } from "./payment-http-gateway";

const apiGetMock = vi.mocked(apiGet);
const apiPostMock = vi.mocked(apiPost);
const apiPutMock = vi.mocked(apiPut);
const apiDeleteMock = vi.mocked(apiDelete);

describe("paymentHttpGateway", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("lists payments and reads status/balance", async () => {
    apiGetMock.mockResolvedValueOnce([
      { idPayment: 1, amount: 100, fkIdBudget: { idBudget: 9 }, date: "2026-01-01" },
    ]);
    await expect(paymentHttpGateway.list()).resolves.toEqual([
      {
        idPayment: 1,
        idBudget: 9,
        amount: 100,
        date: "2026-01-01",
        fkIdBudget: { idBudget: 9 },
      },
    ]);
    expect(apiGetMock).toHaveBeenCalledWith("/payments");

    apiGetMock.mockResolvedValueOnce("PARTIAL");
    await expect(paymentHttpGateway.getStatus(9)).resolves.toBe("PARTIAL");
    expect(apiGetMock).toHaveBeenCalledWith("/payments/budget/9/status");

    apiGetMock.mockResolvedValueOnce(250);
    await expect(paymentHttpGateway.getBalance(9)).resolves.toBe(250);
  });

  it("creates with idBudget, updates without it, deletes", async () => {
    apiPostMock.mockResolvedValue(undefined);
    await paymentHttpGateway.create({
      idBudget: 7,
      amount: 50,
      paymentMethod: "EFECTIVO",
      notes: "n",
    });
    expect(apiPostMock).toHaveBeenCalledWith("/payments", {
      idBudget: 7,
      amount: 50,
      paymentMethod: "EFECTIVO",
      notes: "n",
    });

    apiPutMock.mockResolvedValue(undefined);
    await paymentHttpGateway.update(3, { amount: 60, idBudget: 7, notes: "u" });
    expect(apiPutMock).toHaveBeenCalledWith("/payments/3", {
      amount: 60,
      notes: "u",
    });

    apiDeleteMock.mockResolvedValue(undefined);
    await paymentHttpGateway.delete(3);
    expect(apiDeleteMock).toHaveBeenCalledWith("/payments/3");
  });
});
