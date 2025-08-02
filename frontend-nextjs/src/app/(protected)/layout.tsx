import { redirect } from "next/navigation";
import { getSession } from "@/lib/auth/server";
import { SessionProvider } from "@/components/auth/SessionProvider";

// Use revalidate instead of force-dynamic for better performance
// This allows caching while still checking auth on each request
export const revalidate = 0;

export default async function ProtectedLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // Check authentication on the server
  const session = await getSession();

  if (!session?.authenticated) {
    // If not authenticated, redirect to login
    redirect("/login");
  }

  return <SessionProvider initialSession={session}>{children}</SessionProvider>;
}
