import Link from "next/link";
import { getSession } from "@/lib/auth/server";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";

// Force dynamic rendering since we use cookies
export const dynamic = "force-dynamic";

export default async function Home() {
  const session = await getSession();

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1 text-center">
          <CardTitle className="text-2xl font-bold">Voenix Shop</CardTitle>
          <CardDescription>Custom mug e-commerce admin panel</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {session?.authenticated ? (
            <div className="space-y-4">
              <p className="text-sm text-gray-600">
                Welcome back, {session.user?.email}!
              </p>
              <Link href="/admin">
                <Button className="w-full">Go to Admin Panel</Button>
              </Link>
            </div>
          ) : (
            <div className="space-y-4">
              <p className="text-sm text-gray-600">
                Please log in to access the admin panel.
              </p>
              <Link href="/login">
                <Button className="w-full">Log In</Button>
              </Link>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
