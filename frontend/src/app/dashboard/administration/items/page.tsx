import { redirect } from "next/navigation";

/** Legacy duplicate — canonical route is `/dashboard/items` (#1058). */
export default function AdministrationItemsRedirectPage() {
  redirect("/dashboard/items");
}
