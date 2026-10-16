// @vitest-environment node
import { describe, expect, it } from "vitest";
import type { RegistrationDraftGateway } from "./ports";
import { createRegistrationDraftUseCases } from "./use-cases";

describe("createRegistrationDraftUseCases", () => {
  it("delegates generate/submit/downloadReport", async () => {
    const calls: unknown[] = [];
    const draft = { idRegistrationDraft: 1, idDeed: 9 };
    const gw: RegistrationDraftGateway = {
      getById: async (id) => {
        calls.push(["get", id]);
        return draft;
      },
      generate: async (idDeed) => {
        calls.push(["generate", idDeed]);
        return draft;
      },
      submit: async (id, input) => {
        calls.push(["submit", id, input]);
        return draft;
      },
      observe: async (id, input) => {
        calls.push(["observe", id, input]);
        return draft;
      },
      register: async (id, input) => {
        calls.push(["register", id, input]);
        return draft;
      },
      downloadReport: async (id) => {
        calls.push(["download", id]);
        return new Blob();
      },
    };
    const uc = createRegistrationDraftUseCases(gw);
    await uc.generate(9);
    await uc.submit(1, { dateSubmission: "2026-01-01", registryEntryNumber: "E-1" });
    await uc.downloadReport(1);
    expect(calls).toEqual([
      ["generate", 9],
      ["submit", 1, { dateSubmission: "2026-01-01", registryEntryNumber: "E-1" }],
      ["download", 1],
    ]);
  });
});
