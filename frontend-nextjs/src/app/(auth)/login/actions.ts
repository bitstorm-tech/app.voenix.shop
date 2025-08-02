"use server";

import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { z } from "zod";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

// Validation schema for login form
const loginSchema = z.object({
  email: z.string().email("Please enter a valid email address"),
  password: z.string().min(1, "Password is required"),
});

export type LoginFormState = {
  success: boolean;
  errors?: {
    email?: string[];
    password?: string[];
    form?: string[];
  };
};

export async function loginAction(
  prevState: LoginFormState,
  formData: FormData,
): Promise<LoginFormState> {
  // Validate form data
  const validatedFields = loginSchema.safeParse({
    email: formData.get("email"),
    password: formData.get("password"),
  });

  if (!validatedFields.success) {
    return {
      success: false,
      errors: validatedFields.error.flatten().fieldErrors,
    };
  }

  const { email, password } = validatedFields.data;

  try {
    // Make login request to backend
    const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        return {
          success: false,
          errors: {
            form: ["Invalid email or password"],
          },
        };
      } else {
        const errorData = await response.json().catch(() => ({}));
        return {
          success: false,
          errors: {
            form: [errorData.message || "An error occurred during login"],
          },
        };
      }
    }

    // Extract session cookie from response
    const setCookieHeader = response.headers.get("set-cookie");

    if (setCookieHeader) {
      // Parse the JSESSIONID cookie
      const sessionCookieMatch = setCookieHeader.match(/JSESSIONID=([^;]+)/);

      if (sessionCookieMatch) {
        const sessionId = sessionCookieMatch[1];
        const cookieStore = await cookies();

        // Set the session cookie
        cookieStore.set("JSESSIONID", sessionId, {
          httpOnly: true,
          secure: process.env.NODE_ENV === "production",
          sameSite: "lax",
          path: "/",
        });
      }
    }

    return { success: true };
  } catch (error) {
    console.error("Login error:", error);
    return {
      success: false,
      errors: {
        form: ["Network error. Please try again."],
      },
    };
  }
}

export async function redirectAfterLogin() {
  redirect("/admin");
}
