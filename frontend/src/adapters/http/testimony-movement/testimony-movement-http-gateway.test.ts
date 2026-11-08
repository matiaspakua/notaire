// @vitest-environment node
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/api-client", () => ({
  apiPost: vi.fn(),
}));

import { apiPost } from "@/lib/api-client";
import { testimonyMovementHttpGateway } from "./testimony-movement-http-gateway";

describe("testimonyMovementHttpGateway", () => {
  beforeEach(() => vi.clearAllMocks());

  it("posts movement actions with English paths and cardNumber body", async () => {
    vi.mocked(apiPost).mockResolvedValue({ idTestimonyMovement: 1 });
    await testimonyMovementHttpGateway.enterRegistration(5);
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/enter-registration", {});

    await testimonyMovementHttpGateway.withdraw({ idTestimony: 5, cardNumber: 99 });
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/withdraw", { cardNumber: 99 });

    await testimonyMovementHttpGateway.reenter({
      idTestimony: 5,
      cardNumber: 99,
      observedByRegistry: false,
      notes: "",
    });
    expect(apiPost).toHaveBeenCalledWith("/testimony-movements/5/reenter", {
      cardNumber: 99,
      observedByRegistry: false,
      notes: "",
    });
  });
});
