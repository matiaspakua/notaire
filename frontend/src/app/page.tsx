import { redirect } from "next/navigation";

// Root → always redirect to login; middleware handles auth guards
export default function RootPage() {
  redirect("/login");
}
