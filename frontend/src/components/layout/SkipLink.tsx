"use client";

import { useTranslations } from "next-intl";

/**
 * "Skip to content" link (#1352, WCAG 2.4.1 Bypass Blocks). Visually hidden
 * until it receives keyboard focus; activating it moves focus to the target
 * (a `tabIndex={-1}` landmark) without changing the URL hash.
 */
export function SkipLink({ targetId }: { targetId: string }) {
  const t = useTranslations("navigation");
  return (
    <a
      href={`#${targetId}`}
      data-testid="skip-link"
      onClick={(e) => {
        const target = document.getElementById(targetId);
        if (!target) return;
        e.preventDefault();
        target.focus();
      }}
      className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[100] focus:rounded-[10px] focus:bg-primary focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:text-primary-foreground focus:shadow-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
    >
      {t("skipToContent")}
    </a>
  );
}
