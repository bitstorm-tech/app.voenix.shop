import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/server";

export async function GET() {
  try {
    const session = await getSession();

    if (!session) {
      return NextResponse.json(
        { authenticated: false, roles: [] },
        { 
          status: 401,
          headers: {
            // Cache for 5 seconds to reduce backend calls
            'Cache-Control': 'private, max-age=5',
          }
        },
      );
    }

    return NextResponse.json(session, { 
      status: 200,
      headers: {
        // Cache successful auth checks for 30 seconds
        'Cache-Control': 'private, max-age=30',
      }
    });
  } catch (error) {
    console.error("Session check error:", error);
    return NextResponse.json(
      { authenticated: false, roles: [] },
      { 
        status: 401,
        headers: {
          // Don't cache errors
          'Cache-Control': 'no-store',
        }
      },
    );
  }
}
