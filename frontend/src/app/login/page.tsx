"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Scale, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { useAuthStore } from "@/store/auth-store";
import { apiPost } from "@/lib/api-client";
import type { DtoUsuario } from "@/types";

export default function LoginPage() {
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

      if (result.valido) {
        login(result);
        // Set a cookie so middleware can detect auth on server
        document.cookie = "notaire-auth-status=1; path=/; SameSite=Lax";
        toast.success(`Bienvenido, ${result.nombre}`);
        router.push("/dashboard");
      } else {
        toast.error("Usuario o contraseña incorrectos");
      }
    } catch {
      toast.error("No se pudo conectar al servidor. Verifique que el backend esté en ejecución.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800 p-4">
      <Card className="w-full max-w-sm shadow-2xl">
        <CardHeader className="text-center pb-2">
          <div className="flex justify-center mb-3">
            <div className="bg-amber-400 p-3 rounded-full">
              <Scale className="h-8 w-8 text-slate-900" />
            </div>
          </div>
          <CardTitle className="text-2xl">Notaire</CardTitle>
          <CardDescription>Sistema de Gestión de Escribanía</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="nombre">Usuario</Label>
              <Input
                id="nombre"
                data-testid="input-usuario"
                placeholder="nombre de usuario"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                autoComplete="username"
                disabled={loading}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="contrasenia">Contraseña</Label>
              <Input
                id="contrasenia"
                data-testid="input-contrasenia"
                type="password"
                placeholder="••••••••"
                value={contrasenia}
                onChange={(e) => setContrasenia(e.target.value)}
                autoComplete="current-password"
                disabled={loading}
              />
            </div>
            <Button
              type="submit"
              className="w-full"
              disabled={loading}
              data-testid="btn-ingresar"
            >
              {loading && <Loader2 className="h-4 w-4 animate-spin" />}
              {loading ? "Iniciando sesión..." : "Ingresar"}
            </Button>
          </form>
          <p className="text-xs text-muted-foreground text-center mt-4">
            Backend: {process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1"}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
