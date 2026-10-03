import { redirect } from "next/navigation";

// Root → always redirect to login; edge proxy handles auth guards
export default function RootPage() {
  redirect("/login");
}
