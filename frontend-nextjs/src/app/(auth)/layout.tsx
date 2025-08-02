import { isAuthenticated } from "@/lib/auth/server";
import { redirect } from "next/navigation";

// Use revalidate for better performance
export const revalidate = 0;

export default async function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // Check if user is already authenticated
  const authenticated = await isAuthenticated();

  if (authenticated) {
    // If already authenticated, redirect to admin panel
    redirect("/admin");
  }

  return <div className="min-h-screen bg-gray-50">{children}</div>;
}
