import { redirect } from "next/navigation";

/**
 * Top-level entry point for the business audit log.
 *
 * The canonical implementation lives under
 * {@code /dashboard/auditoria} where it inherits the dashboard
 * layout (header, navigation, theme). Visiting {@code /auditoria}
 * directly redirects there so external links remain stable.
 */
export default function AuditoriaPage() {
  redirect("/dashboard/auditoria");
}
