import { redirect } from "next/navigation";

/** Legacy duplicate — canonical route is `/dashboard/audit` (#1058). */
export default function AdministracionAuditoriaRedirectPage() {
  redirect("/dashboard/audit");
}
