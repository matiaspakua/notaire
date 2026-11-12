/**
 * MfaEnrollmentPanel (#1640, ADR-032): begin, show secret + otpauth URI, confirm, recovery codes once.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { FakeApiError, fakeUseTranslations, routePosts } from "./mfa-test-support";

vi.mock("next-intl", () => ({ useTranslations: fakeUseTranslations }));
vi.mock("@/lib/api-client", () => ({ apiPost: vi.fn(), ApiError: FakeApiError }));

import { apiPost } from "@/lib/api-client";
import { MfaEnrollmentPanel } from "@/components/mfa/mfa-enrollment-panel";

const post = vi.mocked(apiPost) as unknown as ReturnType<typeof vi.fn>;
const START = { secret: "JBSWY3DPEHPK3PXP", otpauthUri: "otpauth://totp/Notaire:a?secret=JBSWY3DPEHPK3PXP" };

beforeEach(() => vi.clearAllMocks());

async function reachConfirmStep(onDone = vi.fn(), bearerToken?: string) {
  render(<MfaEnrollmentPanel onDone={onDone} bearerToken={bearerToken} />);
  await screen.findByTestId("mfa-secret");
  return onDone;
}

describe("MfaEnrollmentPanel", () => {
  it("shows a loading state while enrolment starts", async () => {
    post.mockReturnValue(new Promise(() => {}));
    render(<MfaEnrollmentPanel onDone={vi.fn()} />);
    expect(screen.getByTestId("mfa-loading")).toBeInTheDocument();
  });

  it("begins with the bearer token and shows the secret and otpauth link", async () => {
    routePosts(post, { "/mfa/enrollment": () => START });
    await reachConfirmStep(vi.fn(), "scoped");
    expect(post).toHaveBeenCalledWith("/mfa/enrollment", {}, { bearerToken: "scoped" });
    expect(screen.getByTestId("mfa-secret")).toHaveTextContent(START.secret);
    expect(screen.getByTestId("mfa-otpauth-link")).toHaveAttribute("href", START.otpauthUri);
  });

  it.each([
    [409, "mfa.alreadyEnrolled"],
    [429, "mfa.locked"],
    [500, "mfa.beginError"],
  ])("maps a %s begin failure to %s with a retry action", async (status, key) => {
    routePosts(post, {
      "/mfa/enrollment": () => {
        throw new FakeApiError(status, "/mfa/enrollment", "");
      },
    });
    render(<MfaEnrollmentPanel onDone={vi.fn()} />);
    expect(await screen.findByTestId("mfa-error")).toHaveTextContent(key);
    routePosts(post, { "/mfa/enrollment": () => START });
    fireEvent.click(screen.getByTestId("btn-mfa-retry"));
    expect(await screen.findByTestId("mfa-secret")).toBeInTheDocument();
  });

  it("does not send a malformed confirmation code", async () => {
    routePosts(post, { "/mfa/enrollment": () => START });
    await reachConfirmStep();
    fireEvent.change(screen.getByTestId("input-mfa-confirm-code"), { target: { value: "12" } });
    fireEvent.click(screen.getByTestId("btn-mfa-confirm"));
    expect(await screen.findByTestId("mfa-error")).toHaveTextContent("mfa.codeInvalid");
    expect(post).toHaveBeenCalledTimes(1);
  });

  it("keeps the form editable after a wrong confirmation code (400)", async () => {
    routePosts(post, {
      "/mfa/enrollment": () => START,
      "/mfa/enrollment/confirm": () => {
        throw new FakeApiError(400, "/mfa/enrollment/confirm", "");
      },
    });
    await reachConfirmStep();
    fireEvent.change(screen.getByTestId("input-mfa-confirm-code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-confirm"));
    const error = await screen.findByTestId("mfa-error");
    expect(error).toHaveTextContent("mfa.confirmWrong");
    const input = screen.getByTestId("input-mfa-confirm-code");
    expect(input).toHaveAttribute("aria-invalid", "true");
    expect(input.getAttribute("aria-describedby")?.split(" ")).toContain(error.id);
    expect(input).not.toBeDisabled();
    expect(screen.getByTestId("mfa-secret")).toBeInTheDocument();
  });

  it("shows recovery codes once and gates done on acknowledgement", async () => {
    routePosts(post, {
      "/mfa/enrollment": () => START,
      "/mfa/enrollment/confirm": () => ({ recoveryCodes: ["AAAAA11111", "BBBBB22222"] }),
    });
    const onDone = await reachConfirmStep();
    fireEvent.change(screen.getByTestId("input-mfa-confirm-code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-confirm"));

    const list = await screen.findByTestId("mfa-recovery-codes");
    expect(list).toHaveTextContent("AAAAA11111");
    expect(list).toHaveTextContent("BBBBB22222");
    expect(screen.queryByTestId("mfa-secret")).not.toBeInTheDocument();
    expect(screen.getByTestId("btn-mfa-done")).toBeDisabled();

    fireEvent.click(screen.getByTestId("check-mfa-saved"));
    await waitFor(() => expect(screen.getByTestId("btn-mfa-done")).not.toBeDisabled());
    fireEvent.click(screen.getByTestId("btn-mfa-done"));
    expect(onDone).toHaveBeenCalledTimes(1);
  });
});
