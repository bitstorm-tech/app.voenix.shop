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
  // The middleware already protects these routes, so a redirect here is redundant.
  // We still call getSession() to pass the session data to the client-side
  // SessionProvider.
  const session = await getSession();

  return <SessionProvider initialSession={session}>{children}</SessionProvider>;
}
