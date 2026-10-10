import { redirect } from "next/navigation";

/**
 * Top-level entry point for the business audit log.
 *
 * The canonical implementation lives under
 * {@code /dashboard/audit} where it inherits the dashboard
 * layout (header, navigation, theme). Visiting {@code /audit}
 * directly redirects there so external links remain stable.
 */
export default function AuditoriaPage() {
  redirect("/dashboard/audit");
}
