// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { AuxiliaryFolio, StartAuxiliaryDeedInput } from "../domain/auxiliary-protocol";
import type { AuxiliaryProtocolGateway } from "./ports";
import { createAuxiliaryProtocolUseCases } from "./use-cases";

describe("auxiliary protocol use cases (#1833)", () => {
  it("lists available folios and starts a deed with idFolio+body", async () => {
    const folios: AuxiliaryFolio[] = [{ idFolio: 3, number: 10, status: "Nuevo" }];
    let started: StartAuxiliaryDeedInput | undefined;
    const gateway: AuxiliaryProtocolGateway = {
      listAvailableFolios: async () => folios,
      startDeed: async (input) => {
        started = input;
      },
    };
    const uc = createAuxiliaryProtocolUseCases(gateway);
    await expect(uc.listAvailableFolios()).resolves.toEqual(folios);
    await uc.startDeed({ idFolio: 3, body: "Deed body" });
    expect(started).toEqual({ idFolio: 3, body: "Deed body" });
  });
});
