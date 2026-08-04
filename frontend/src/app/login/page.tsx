"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Scale, Loader2 } from "lucide-react";
import { motion } from "motion/react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { useAuthStore } from "@/store/auth-store";
import { apiPost, ApiError } from "@/lib/api-client";
import type { DtoUsuario } from "@/types";

const GENERIC_CONNECTION_ERROR =
  "No se pudo conectar al servidor. Verifique que el backend esté en ejecución.";
const GENERIC_LOCKOUT_ERROR = "Cuenta bloqueada temporalmente por demasiados intentos fallidos.";

/** Reads the backend's `message` field from a lockout response body, if present. */
function lockoutMessage(body: string): string {
  try {
    const parsed = JSON.parse(body) as { message?: string };
    return parsed.message?.trim() || GENERIC_LOCKOUT_ERROR;
  } catch {
    return GENERIC_LOCKOUT_ERROR;
  }
}

export default function LoginPage() {
  const t = useTranslations("login");
  const router = useRouter();
  const { login } = useAuthStore();
  const [nombre, setNombre] = useState("");
  const [contrasenia, setContrasenia] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!nombre.trim() || !contrasenia.trim()) {
      toast.error("Complete usuario y contraseña");
      return;
    }
    setLoading(true);
    try {
      const result = await apiPost<DtoUsuario>("/usuarios/login", {
        nombre: nombre.trim(),
        contrasenia: contrasenia.trim(),
      });

      if (result.valido && result.token) {
        login(result, result.token);
        document.cookie = "notaire-auth-status=1; path=/; SameSite=Lax";
        toast.success(`Bienvenido, ${result.nombre}`);
        router.push("/dashboard");
      } else {
        toast.error(t("error"));
      }
    } catch (error) {
      if (error instanceof ApiError && error.status === 429) {
        toast.error(lockoutMessage(error.body));
      } else {
        toast.error(GENERIC_CONNECTION_ERROR);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-6">
      <div className="w-full max-w-[400px] space-y-8 animate-in fade-in duration-700">
        <div className="text-center space-y-4">
          <div className="flex justify-center">
            <motion.div
              className="bg-primary/10 p-4 rounded-[22px] shadow-sm"
              initial={{ scale: 0.8, opacity: 0, rotate: -8 }}
              animate={{ scale: 1, opacity: 1, rotate: 0 }}
              transition={{ type: "spring", stiffness: 260, damping: 18 }}
            >
              <Scale className="h-10 w-10 text-primary" />
            </motion.div>
          </div>
          <div className="space-y-2">
            <h1 className="text-4xl font-semibold tracking-tight text-foreground">Notaire</h1>
            <p className="text-lg text-muted-foreground font-medium">{t("subtitle")}</p>
            <p className="text-sm text-muted-foreground">{t("title")}</p>
          </div>
        </div>

        <Card className="border-none shadow-none bg-transparent">
          <CardContent className="p-0">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="nombre" className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                    {t("username")}
                  </Label>
                  <Input
                    id="nombre"
                    data-testid="input-usuario"
                    placeholder={t("username").toLowerCase()}
                    className="h-12 rounded-[12px] bg-white text-lg apple-focus"
                    value={nombre}
                    onChange={(e) => setNombre(e.target.value)}
                    autoComplete="username"
                    disabled={loading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="contrasenia" className="text-xs font-semibold uppercase tracking-wider text-muted-foreground ml-1">
                    {t("password")}
                  </Label>
                  <Input
                    id="contrasenia"
                    data-testid="input-contrasenia"
                    type="password"
                    placeholder="••••••••"
                    className="h-12 rounded-[12px] bg-white text-lg apple-focus"
                    value={contrasenia}
                    onChange={(e) => setContrasenia(e.target.value)}
                    autoComplete="current-password"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="space-y-4 pt-2">
                <Button
                  type="submit"
                  size="lg"
                  variant="apple"
                  className="w-full h-12 text-lg font-semibold rounded-[12px]"
                  disabled={loading}
                  data-testid="btn-ingresar"
                >
                  {loading && <Loader2 className="h-5 w-5 animate-spin mr-2" />}
                  {loading ? "..." : t("submit")}
                </Button>

                <p className="text-sm text-muted-foreground text-center font-medium">
                  ¿Olvidó su contraseña? Contacte al administrador.
                </p>
              </div>
            </form>
          </CardContent>
        </Card>

        <div className="pt-12 text-center border-t border-border">
          <p className="text-xs font-semibold text-muted-foreground uppercase tracking-widest">
            Infraestructura Segura
          </p>
          <p className="text-[10px] text-muted-foreground/60 mt-1">
            Backend: {process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1"}
          </p>
        </div>
      </div>
    </div>
  );
}
