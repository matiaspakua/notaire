/**
 * Unit tests for LanguageSwitcher component
 * Covers: l10n Phase 3 — language selector UI
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";

const mockSetLocale = vi.fn();
const mockWindowReload = vi.fn();

vi.mock("@/i18n/actions", () => ({
  setLocale: () => mockSetLocale(),
}));

vi.mock("next-intl", () => ({
  useLocale: vi.fn(() => "es"),
  useTranslations: () => (key: string) => key,
}));

import { LanguageSwitcher } from "@/components/shared/LanguageSwitcher";
import { useLocale } from "next-intl";

beforeEach(() => {
  vi.clearAllMocks();
  Object.defineProperty(window, "location", {
    value: { reload: mockWindowReload },
    writable: true,
  });
});

describe("LanguageSwitcher", () => {
  it("renders both ES and EN buttons", () => {
    render(<LanguageSwitcher />);
    expect(screen.getByTestId("locale-es")).toBeDefined();
    expect(screen.getByTestId("locale-en")).toBeDefined();
  });

  it("has correct data-testid on container", () => {
    render(<LanguageSwitcher />);
    expect(screen.getByTestId("language-switcher")).toBeDefined();
  });

  it("marks ES button as aria-pressed=true when locale is es", () => {
    vi.mocked(useLocale).mockReturnValue("es");
    render(<LanguageSwitcher />);
    const esBtn = screen.getByTestId("locale-es");
    expect(esBtn.getAttribute("aria-pressed")).toBe("true");
  });

  it("marks EN button as aria-pressed=false when locale is es", () => {
    vi.mocked(useLocale).mockReturnValue("es");
    render(<LanguageSwitcher />);
    const enBtn = screen.getByTestId("locale-en");
    expect(enBtn.getAttribute("aria-pressed")).toBe("false");
  });

  it("marks EN button as aria-pressed=true when locale is en", () => {
    vi.mocked(useLocale).mockReturnValue("en");
    render(<LanguageSwitcher />);
    const enBtn = screen.getByTestId("locale-en");
    expect(enBtn.getAttribute("aria-pressed")).toBe("true");
  });

  it("marks ES button as aria-pressed=false when locale is en", () => {
    vi.mocked(useLocale).mockReturnValue("en");
    render(<LanguageSwitcher />);
    const esBtn = screen.getByTestId("locale-es");
    expect(esBtn.getAttribute("aria-pressed")).toBe("false");
  });

  it("displays flag emojis for both locales", () => {
    render(<LanguageSwitcher />);
    expect(screen.getByText("🇦🇷")).toBeDefined();
    expect(screen.getByText("🇺🇸")).toBeDefined();
  });

  it("displays language labels ES and EN", () => {
    render(<LanguageSwitcher />);
    expect(screen.getByText("ES")).toBeDefined();
    expect(screen.getByText("EN")).toBeDefined();
  });

  it("calls setLocale when EN button is clicked", async () => {
    vi.mocked(useLocale).mockReturnValue("es");
    render(<LanguageSwitcher />);
    const enBtn = screen.getByTestId("locale-en");
    fireEvent.click(enBtn);
    await vi.waitFor(() => expect(mockSetLocale).toHaveBeenCalled());
  });

  it("calls setLocale when ES button is clicked", async () => {
    vi.mocked(useLocale).mockReturnValue("en");
    render(<LanguageSwitcher />);
    const esBtn = screen.getByTestId("locale-es");
    fireEvent.click(esBtn);
    await vi.waitFor(() => expect(mockSetLocale).toHaveBeenCalled());
  });
});
