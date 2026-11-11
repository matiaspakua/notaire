/**
 * LoginPage MFA steps (#1640, ADR-032): challenge with TOTP / recovery code, enrolment required.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { FakeApiError, fakeUseTranslations, routePosts } from "./mfa-test-support";

const push = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push, replace: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
}));
vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));
vi.mock("next-intl", () => ({ useTranslations: fakeUseTranslations, useLocale: () => "es" }));
vi.mock("@/lib/api-client", () => ({ apiPost: vi.fn(), ApiError: FakeApiError }));

import { toast } from "sonner";
import LoginPage from "@/app/login/page";
import { apiPost } from "@/lib/api-client";
import { useAuthStore } from "@/store/auth-store";

const post = vi.mocked(apiPost) as unknown as ReturnType<typeof vi.fn>;
const CHALLENGE = { valid: false, mfaRequired: true, mfaEnrollmentRequired: false, mfaToken: "ch" };
const SESSION = { valid: true, name: "alice", type: "ADMIN" };

beforeEach(() => {
  vi.clearAllMocks();
  useAuthStore.setState({ user: null, token: null, isAuthenticated: false });
});

async function passPasswordStep() {
  fireEvent.change(screen.getByTestId("input-username"), { target: { value: "alice" } });
  fireEvent.change(screen.getByTestId("input-password"), { target: { value: "pw" } });
  fireEvent.click(screen.getByTestId("btn-sign-in"));
}

async function reachChallenge(loginMfa: (b: unknown) => unknown = () => SESSION) {
  routePosts(post, { "/users/login": () => CHALLENGE, "/users/login/mfa": loginMfa });
  render(<LoginPage />);
  await passPasswordStep();
  return screen.findByTestId("input-mfa-code");
}

describe("LoginPage MFA challenge", () => {
  it("shows the code step instead of creating a session", async () => {
    await reachChallenge();
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(push).not.toHaveBeenCalled();
    expect(screen.queryByTestId("btn-sign-in")).not.toBeInTheDocument();
  });

  it("completes login with a TOTP and only then stores the session", async () => {
    const input = await reachChallenge();
    fireEvent.change(input, { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-verify"));
    await waitFor(() => expect(push).toHaveBeenCalledWith("/dashboard"));
    expect(post).toHaveBeenCalledWith("/users/login/mfa", { mfaToken: "ch", code: "123456" });
    expect(useAuthStore.getState().isAuthenticated).toBe(true);
  });

  it("sends a recovery code as recoveryCode", async () => {
    const input = await reachChallenge();
    expect(input).toHaveAttribute("inputmode", "numeric");
    fireEvent.click(screen.getByTestId("toggle-mfa-recovery"));
    fireEvent.change(screen.getByTestId("input-mfa-code"), { target: { value: "abcde-12345" } });
    fireEvent.click(screen.getByTestId("btn-mfa-verify"));
    await waitFor(() => expect(push).toHaveBeenCalled());
    expect(post).toHaveBeenCalledWith("/users/login/mfa", { mfaToken: "ch", recoveryCode: "ABCDE12345" });
  });

  it("reports a wrong code, clears the field and keeps the step", async () => {
    const input = await reachChallenge(() => ({ valid: false }));
    fireEvent.change(input, { target: { value: "000000" } });
    fireEvent.click(screen.getByTestId("btn-mfa-verify"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("login.mfaError"));
    expect(screen.getByTestId("input-mfa-code")).toHaveValue("");
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
    expect(push).not.toHaveBeenCalled();
  });

  it("shows the lockout message on 429", async () => {
    const input = await reachChallenge(() => {
      throw new FakeApiError(429, "/users/login/mfa", JSON.stringify({ message: "Locked now" }));
    });
    fireEvent.change(input, { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-verify"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("Locked now"));
  });

  it("does not send a malformed TOTP", async () => {
    const input = await reachChallenge();
    fireEvent.change(input, { target: { value: "12" } });
    fireEvent.click(screen.getByTestId("btn-mfa-verify"));
    await waitFor(() => expect(toast.error).toHaveBeenCalledWith("login.mfaCodeInvalid"));
    expect(post).toHaveBeenCalledTimes(1);
  });

  it("returns to the password step from the challenge", async () => {
    await reachChallenge();
    fireEvent.click(screen.getByTestId("btn-mfa-back"));
    expect(await screen.findByTestId("btn-sign-in")).toBeInTheDocument();
  });

  it("still logs in directly when no second factor is required", async () => {
    routePosts(post, { "/users/login": () => SESSION });
    render(<LoginPage />);
    await passPasswordStep();
    await waitFor(() => expect(push).toHaveBeenCalledWith("/dashboard"));
  });
});

describe("LoginPage enrolment required", () => {
  it("enrols with the scoped token, then returns to the password step", async () => {
    routePosts(post, {
      "/users/login": () => ({ valid: false, mfaEnrollmentRequired: true, mfaToken: "en" }),
      "/mfa/enrollment": () => ({ secret: "S", otpauthUri: "otpauth://x" }),
      "/mfa/enrollment/confirm": () => ({ recoveryCodes: ["AAAAA11111"] }),
    });
    render(<LoginPage />);
    await passPasswordStep();
    await screen.findByTestId("mfa-secret");
    expect(post).toHaveBeenCalledWith("/mfa/enrollment", {}, { bearerToken: "en" });
    expect(useAuthStore.getState().isAuthenticated).toBe(false);

    fireEvent.change(screen.getByTestId("input-mfa-confirm-code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-confirm"));
    await screen.findByTestId("mfa-recovery-codes");
    fireEvent.click(screen.getByTestId("check-mfa-saved"));
    fireEvent.click(screen.getByTestId("btn-mfa-done"));
    expect(await screen.findByTestId("btn-sign-in")).toBeInTheDocument();
    expect(push).not.toHaveBeenCalled();
  });
});
