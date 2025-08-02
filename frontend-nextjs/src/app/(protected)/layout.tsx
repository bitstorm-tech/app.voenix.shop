import { redirect } from "next/navigation";
import { getSession } from "@/lib/auth/server";
import { SessionProvider } from "@/components/auth/SessionProvider";

// Force dynamic rendering since we use cookies
export const dynamic = "force-dynamic";

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
