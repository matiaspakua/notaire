/**
 * /dashboard/security (#1640, ADR-032): enrol on the session cookie, disable with a current code.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { FakeApiError, fakeUseTranslations, routePosts } from "./mfa-test-support";

vi.mock("next-intl", () => ({ useTranslations: fakeUseTranslations }));
vi.mock("@/lib/api-client", () => ({ apiPost: vi.fn(), ApiError: FakeApiError }));

import { apiPost } from "@/lib/api-client";
import SecurityPage from "@/app/dashboard/security/page";

const post = vi.mocked(apiPost) as unknown as ReturnType<typeof vi.fn>;

beforeEach(() => vi.clearAllMocks());

describe("SecurityPage", () => {
  it("explains the empty state and offers enrolment without starting it", () => {
    render(<SecurityPage />);
    expect(screen.getByTestId("btn-mfa-start")).toBeInTheDocument();
    expect(post).not.toHaveBeenCalled();
  });

  it("enrols on the session cookie (no bearer token)", async () => {
    routePosts(post, { "/mfa/enrollment": () => ({ secret: "S", otpauthUri: "otpauth://x" }) });
    render(<SecurityPage />);
    fireEvent.click(screen.getByTestId("btn-mfa-start"));
    expect(await screen.findByTestId("mfa-secret")).toHaveTextContent("S");
    expect(post).toHaveBeenCalledWith("/mfa/enrollment", {}, { bearerToken: undefined });
  });

  it("disables with a TOTP and reports it", async () => {
    routePosts(post, { "/mfa/disable": () => undefined });
    render(<SecurityPage />);
    fireEvent.change(screen.getByTestId("input-mfa-disable-code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-disable"));
    expect(await screen.findByTestId("mfa-disabled-notice")).toBeInTheDocument();
    expect(post).toHaveBeenCalledWith("/mfa/disable", { code: "123456" });
  });

  it("disables with a recovery code when toggled", async () => {
    routePosts(post, { "/mfa/disable": () => undefined });
    render(<SecurityPage />);
    fireEvent.click(screen.getByTestId("toggle-mfa-disable-recovery"));
    fireEvent.change(screen.getByTestId("input-mfa-disable-code"), { target: { value: "abcde-12345" } });
    fireEvent.click(screen.getByTestId("btn-mfa-disable"));
    await screen.findByTestId("mfa-disabled-notice");
    expect(post).toHaveBeenCalledWith("/mfa/disable", { recoveryCode: "ABCDE12345" });
  });

  it("shows an error on a wrong code (400) and no success notice", async () => {
    routePosts(post, {
      "/mfa/disable": () => {
        throw new FakeApiError(400, "/mfa/disable", "");
      },
    });
    render(<SecurityPage />);
    fireEvent.change(screen.getByTestId("input-mfa-disable-code"), { target: { value: "000000" } });
    fireEvent.click(screen.getByTestId("btn-mfa-disable"));
    expect(await screen.findByTestId("mfa-disable-error")).toHaveTextContent("mfa.disableWrong");
    expect(screen.queryByTestId("mfa-disabled-notice")).not.toBeInTheDocument();
  });

  it("reports not-enrolled (404) distinctly", async () => {
    routePosts(post, {
      "/mfa/disable": () => {
        throw new FakeApiError(404, "/mfa/disable", "");
      },
    });
    render(<SecurityPage />);
    fireEvent.change(screen.getByTestId("input-mfa-disable-code"), { target: { value: "123456" } });
    fireEvent.click(screen.getByTestId("btn-mfa-disable"));
    await waitFor(() =>
      expect(screen.getByTestId("mfa-disable-error")).toHaveTextContent("mfa.notEnrolled"),
    );
  });
});
