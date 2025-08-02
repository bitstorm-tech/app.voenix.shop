import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/server";

export async function GET() {
  try {
    const session = await getSession();

    if (!session) {
      return NextResponse.json(
        { authenticated: false, roles: [] },
        { status: 401 },
      );
    }

    return NextResponse.json(session, { status: 200 });
  } catch (error) {
    console.error("Session check error:", error);
    return NextResponse.json(
      { authenticated: false, roles: [] },
      { status: 401 },
    );
  }
}
