"use client";

import { Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { toast } from "sonner";
import { Scale, Loader2 } from "lucide-react";
import { motion } from "motion/react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormContainer, FormField, FormActions } from "@/theme/form-patterns";
import { useAuthStore } from "@/store/auth-store";
import { authUseCases } from "@/composition/auth";
import type { DtoLoginUser } from "@/types";

/** Duck-type HTTP status/body without importing api-client (ADR-030). */
function httpStatus(err: unknown): number | undefined {
  if (typeof err === "object" && err !== null && "status" in err) {
    const status = (err as { status: unknown }).status;
    return typeof status === "number" ? status : undefined;
  }
  return undefined;
}

function errorBody(err: unknown): string {
  if (typeof err === "object" && err !== null && "body" in err) {
    const body = (err as { body: unknown }).body;
    return typeof body === "string" ? body : "";
  }
  return "";
}

/** Reads the backend's `message` field from a lockout response body, if present. */
function lockoutMessage(body: string, fallback: string): string {
  try {
    const parsed = JSON.parse(body) as { message?: string };
    return parsed.message?.trim() || fallback;
  } catch {
    return fallback;
  }
}

function LoginForm() {
  const t = useTranslations("login");
  const router = useRouter();
  const searchParams = useSearchParams();
  const sessionExpired = searchParams.get("expired") === "1";
  const { login } = useAuthStore();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      toast.error(t("validationRequired"));
      return;
    }
    setLoading(true);
    try {
      const result = (await authUseCases.login({
        name: username.trim(),
        password: password.trim(),
      })) as DtoLoginUser;

      if (result.valid) {
        // UX cookies for edge gating are set inside auth-store.login (#1052).
        // JWT is delivered as HttpOnly Set-Cookie; ignore JSON token (#1051).
        login(result);
        toast.success(t("welcome", { name: result.name }));
        router.push("/dashboard");
      } else {
        toast.error(t("error"));
      }
    } catch (error) {
      if (httpStatus(error) === 429) {
        toast.error(lockoutMessage(errorBody(error), t("lockoutError")));
      } else {
        toast.error(t("connectionError"));
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-6">
      <div className="w-full max-w-[400px] space-y-8 animate-in fade-in-0 duration-slow ease-standard">
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

        {sessionExpired && (
          <p
            data-testid="session-expired-message"
            role="status"
            className="rounded-[12px] border border-warning/30 bg-warning/10 px-4 py-3 text-sm text-warning text-center font-medium"
          >
            {t("sessionExpired")}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <FormContainer>
            <FormField label={t("username")} required>
              <Input
                data-testid="input-username"
                placeholder={t("username").toLowerCase()}
                className="h-12 text-lg"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                disabled={loading}
              />
            </FormField>

            <FormField label={t("password")} required>
              <Input
                data-testid="input-password"
                type="password"
                placeholder="••••••••"
                className="h-12 text-lg"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                disabled={loading}
              />
            </FormField>

            <FormActions align="center">
              <div data-testid="login-form-actions" className="flex w-full flex-col gap-4">
                <Button
                  type="submit"
                  size="lg"
                  variant="apple"
                  className="w-full h-12 text-lg font-semibold"
                  disabled={loading}
                  data-testid="btn-sign-in"
                >
                  {loading && <Loader2 className="h-5 w-5 animate-spin mr-2" />}
                  {loading ? "..." : t("submit")}
                </Button>
                <p className="text-sm text-muted-foreground text-center font-medium">
                  {t("forgotPassword")}
                </p>
              </div>
            </FormActions>
          </FormContainer>
        </form>

        <div className="pt-12 text-center border-t border-border">
          <p className="text-xs font-medium text-muted-foreground">
            {t("footerSecure")}
          </p>
        </div>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginForm />
    </Suspense>
  );
}
