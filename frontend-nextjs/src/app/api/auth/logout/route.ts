import { NextResponse } from "next/server";
import { cookies } from "next/headers";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

export async function POST() {
  try {
    const cookieStore = await cookies();
    const sessionCookie = cookieStore.get("JSESSIONID");

    // Call backend logout endpoint if we have a session
    if (sessionCookie?.value) {
      try {
        await fetch(`${BACKEND_URL}/api/auth/logout`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Cookie: `JSESSIONID=${sessionCookie.value}`,
          },
          credentials: "include",
        });
      } catch (error) {
        // Continue with logout even if backend call fails
        console.error("Backend logout error:", error);
      }
    }

    // Clear the session cookie
    cookieStore.delete("JSESSIONID");

    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error) {
    console.error("Logout error:", error);

    // Even on error, try to clear the cookie and return success
    const cookieStore = await cookies();
    cookieStore.delete("JSESSIONID");

    return NextResponse.json({ success: true }, { status: 200 });
  }
}
