/**
 * Unit tests for LoginPage
 * Covers: CU — Autenticación de usuario / CU84 session expiry (#1053)
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { QueryClientProvider, QueryClient } from "@tanstack/react-query";

let mockSearchParams = new URLSearchParams();

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
  useSearchParams: () => mockSearchParams,
}));

// Mock sonner
vi.mock("sonner", () => ({ toast: { success: vi.fn(), error: vi.fn() } }));

// Mock api-client
vi.mock("@/lib/api-client", () => {
  class ApiError extends Error {
    status: number;
    body: string;
    constructor(status: number, path: string, body: string) {
      super(`[${status}] ${path}: ${body}`);
      this.name = "ApiError";
      this.status = status;
      this.body = body;
    }
  }
  return { apiPost: vi.fn(), ApiError };
});

// Mock next-intl so LoginPage doesn't need NextIntlClientProvider
vi.mock("next-intl", () => ({
  useTranslations: (ns: string) => (key: string, values?: Record<string, string | number>) => {
    const translations: Record<string, Record<string, string>> = {
      login: {
        title: "Iniciar sesión",
        subtitle: "Sistema de Gestión de Escribanía",
        username: "Usuario",
        password: "Contraseña",
        submit: "Ingresar",
        error: "Usuario o contraseña incorrectos",
        sessionExpired: "Su sesión ha expirado. Inicie sesión nuevamente.",
        connectionError:
          "No se pudo conectar al servidor. Verifique que el backend esté en ejecución.",
        lockoutError: "Cuenta bloqueada temporalmente por demasiados intentos fallidos.",
        validationRequired: "Complete usuario y contraseña",
        welcome: "Bienvenido, {name}",
        forgotPassword: "¿Olvidó su contraseña? Contacte al administrador.",
        footerSecure: "Infraestructura Segura",
      },
    };
    const template = translations[ns]?.[key] ?? key;
    if (!values) return template;
    return Object.entries(values).reduce(
      (acc, [k, v]) => acc.replace(`{${k}}`, String(v)),
      template,
    );
  },
  useLocale: () => "es",
}));

import LoginPage from "@/app/login/page";
import { apiPost, ApiError } from "@/lib/api-client";
import { useAuthStore } from "@/store/auth-store";

function wrapper({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={new QueryClient()}>
      {children}
    </QueryClientProvider>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mockSearchParams = new URLSearchParams();
  useAuthStore.setState({ user: null, token: null, isAuthenticated: false });
});

describe("LoginPage", () => {
  it("renders username and password fields", () => {
    render(<LoginPage />, { wrapper });
    expect(screen.getByTestId("input-usuario")).toBeDefined();
    expect(screen.getByTestId("input-contrasenia")).toBeDefined();
  });

  it("renders the login button", () => {
    render(<LoginPage />, { wrapper });
    expect(screen.getByTestId("btn-ingresar")).toBeDefined();
  });

  it("calls apiPost with credentials on submit", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockResolvedValueOnce({ valido: true, nombre: "admin", tipo: "ADMIN" });

    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), {
      target: { value: "admin" },
    });
    fireEvent.change(screen.getByTestId("input-contrasenia"), {
      target: { value: "admin" },
    });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalledWith("/usuarios/login", {
        name: "admin",
        password: "admin",
      });
    });
  });

  it("authenticates without persisting JWT from the login JSON (issue #1051)", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockResolvedValueOnce({
      valido: true,
      nombre: "admin",
      tipo: "ADMIN",
      token: "fake-jwt-token",
    });

    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "admin" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
      expect(useAuthStore.getState().token).toBeNull();
    });
  });

  it("logs in when valido is true even without a JSON token (issue #1051)", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockResolvedValueOnce({ valido: true, nombre: "admin", tipo: "ADMIN" });

    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "admin" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
    });
  });

  it("shows error toast on failed login (valido=false)", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockResolvedValueOnce({ valido: false });

    const { toast } = await import("sonner");
    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "bad" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "bad" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Usuario o contraseña incorrectos");
    });
  });

  it("shows error when fields are empty", async () => {
    const { toast } = await import("sonner");
    render(<LoginPage />, { wrapper });

    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Complete usuario y contraseña");
    });
  });

  it("shows the backend's lockout message on a 429 response (issue #756)", async () => {
    const mockPost = vi.mocked(apiPost);
    const lockoutMessage = "Cuenta bloqueada temporalmente por demasiados intentos fallidos.";
    mockPost.mockRejectedValueOnce(
      new ApiError(429, "/usuarios/login", JSON.stringify({ valido: false, message: lockoutMessage }))
    );

    const { toast } = await import("sonner");
    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "wrong" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(lockoutMessage);
    });
  });

  it("falls back to a generic lockout message when a 429 body has no message field (issue #756)", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockRejectedValueOnce(new ApiError(429, "/usuarios/login", ""));

    const { toast } = await import("sonner");
    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "wrong" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(
        "Cuenta bloqueada temporalmente por demasiados intentos fallidos."
      );
    });
  });

  it("still shows the generic connection-error message for a genuine network failure (issue #756)", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockRejectedValueOnce(new TypeError("Failed to fetch"));

    const { toast } = await import("sonner");
    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "admin" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(
        "No se pudo conectar al servidor. Verifique que el backend esté en ejecución."
      );
    });
  });

  it("shows the session-expired message when expired=1 is in the query string (issue #1053)", () => {
    mockSearchParams = new URLSearchParams("expired=1");
    render(<LoginPage />, { wrapper });
    expect(screen.getByTestId("session-expired-message").textContent).toContain(
      "Su sesión ha expirado"
    );
  });

  it("does not show the session-expired message without expired=1", () => {
    render(<LoginPage />, { wrapper });
    expect(screen.queryByTestId("session-expired-message")).toBeNull();
  });

  it("does not disclose Backend URL or internal API host (issue #1055)", () => {
    process.env.NEXT_PUBLIC_API_URL = "http://backend:8080/api/v1";
    const { container } = render(<LoginPage />, { wrapper });
    const text = container.textContent ?? "";
    expect(text).not.toMatch(/Backend\s*:/i);
    expect(text).not.toContain("backend:8080");
    expect(text).not.toContain("NEXT_PUBLIC_API_URL");
    expect(text).not.toContain("http://localhost:8080/api/v1");
  });
});
