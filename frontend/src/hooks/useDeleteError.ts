/**
 * Delete-failure toasts with the in-use reason (issue #1345).
 * Pages call `showDeleteError(err, t("errorDelete"))` in their delete catch.
 */
import { useCallback } from "react";
import { useTranslations } from "next-intl";
import { presentDeleteError } from "@/lib/mutation-error";

export function useDeleteError() {
  const tc = useTranslations("common");
  return useCallback(
    (err: unknown, fallback: string) =>
      presentDeleteError(err, {
        fallback,
        inUse: tc("errors.inUse"),
        notFound: tc("errors.notFound"),
      }),
    [tc]
  );
}
