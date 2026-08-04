/**
 * Unit tests for LoginPage
 * Covers: CU — Autenticación de usuario
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { QueryClientProvider, QueryClient } from "@tanstack/react-query";

// Mock next/navigation
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn(), replace: vi.fn() }),
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
  useTranslations: (ns: string) => (key: string) => {
    const translations: Record<string, Record<string, string>> = {
      login: {
        title: "Iniciar sesión",
        subtitle: "Sistema de Gestión de Escribanía",
        username: "Usuario",
        password: "Contraseña",
        submit: "Ingresar",
        error: "Usuario o contraseña incorrectos",
      },
    };
    return translations[ns]?.[key] ?? key;
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
        nombre: "admin",
        contrasenia: "admin",
      });
    });
  });

  it("stores the JWT token from the login response (issue #552)", async () => {
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
      expect(useAuthStore.getState().token).toBe("fake-jwt-token");
      expect(useAuthStore.getState().isAuthenticated).toBe(true);
    });
  });

  it("does not log in when the response has no token", async () => {
    const mockPost = vi.mocked(apiPost);
    mockPost.mockResolvedValueOnce({ valido: true, nombre: "admin", tipo: "ADMIN" });

    render(<LoginPage />, { wrapper });

    fireEvent.change(screen.getByTestId("input-usuario"), { target: { value: "admin" } });
    fireEvent.change(screen.getByTestId("input-contrasenia"), { target: { value: "admin" } });
    fireEvent.click(screen.getByTestId("btn-ingresar"));

    await waitFor(() => {
      expect(mockPost).toHaveBeenCalled();
    });
    expect(useAuthStore.getState().isAuthenticated).toBe(false);
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
});
