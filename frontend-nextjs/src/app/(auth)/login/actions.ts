"use server";

import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { z } from "zod";
import { generateCSRFToken } from "@/lib/auth/csrf";

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
    // Note: In server actions, we need to manually handle cookies
    // as 'credentials: include' doesn't work in server-side fetch
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
      // Parse all cookies from the header
      const cookieStore = await cookies();
      
      // Split multiple cookies if present
      const cookieHeaders = setCookieHeader.split(/, (?=[^;]+=[^;]+)/);
      
      for (const cookieHeader of cookieHeaders) {
        // Parse cookie name and value
        const [nameValue, ...attributes] = cookieHeader.split(';');
        const [name, value] = nameValue.trim().split('=');
        
        if (name === 'JSESSIONID') {
          // Parse cookie attributes from backend
          const cookieOptions: any = {
            value,
            httpOnly: true,
            path: '/',
          };
          
          // Parse additional attributes
          for (const attr of attributes) {
            const [key, val] = attr.trim().split('=');
            const lowerKey = key.toLowerCase();
            
            if (lowerKey === 'max-age') {
              cookieOptions.maxAge = parseInt(val);
            } else if (lowerKey === 'domain') {
              cookieOptions.domain = val;
            } else if (lowerKey === 'secure') {
              cookieOptions.secure = true;
            } else if (lowerKey === 'samesite') {
              cookieOptions.sameSite = val.toLowerCase() as 'lax' | 'strict' | 'none';
            }
          }
          
          // Override security settings based on environment
          if (process.env.NODE_ENV === 'production') {
            cookieOptions.secure = true;
          }
          
          // Set the cookie with parsed options
          cookieStore.set('JSESSIONID', cookieOptions.value, {
            httpOnly: cookieOptions.httpOnly,
            secure: cookieOptions.secure || false,
            sameSite: cookieOptions.sameSite || 'lax',
            path: cookieOptions.path,
            ...(cookieOptions.maxAge && { maxAge: cookieOptions.maxAge }),
            ...(cookieOptions.domain && { domain: cookieOptions.domain }),
          });
        }
      }
    }

    // Generate CSRF token for the session
    await generateCSRFToken();

    return { success: true };
  } catch (error) {
    // Improved error handling with specific error types
    if (error instanceof TypeError && error.message.includes('fetch')) {
      console.error("Network error connecting to backend:", error);
      return {
        success: false,
        errors: {
          form: ["Unable to connect to server. Please check your connection and try again."],
        },
      };
    }
    
    console.error("Unexpected login error:", error);
    return {
      success: false,
      errors: {
        form: ["An unexpected error occurred. Please try again."],
      },
    };
  }
}

export async function redirectAfterLogin() {
  redirect("/admin");
}
