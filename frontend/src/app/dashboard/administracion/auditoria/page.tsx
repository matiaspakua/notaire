import { redirect } from "next/navigation";

/** Legacy duplicate — canonical route is `/dashboard/auditoria` (#1058). */
export default function AdministracionAuditoriaRedirectPage() {
  redirect("/dashboard/auditoria");
}
