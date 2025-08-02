import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/Card";
import { getSession } from "@/lib/auth/server";

// Force dynamic rendering since we use cookies
export const dynamic = "force-dynamic";

export default async function AdminDashboard() {
  const session = await getSession();

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">Admin Dashboard</h1>
        <p className="text-gray-600 mt-2">
          Welcome back, {session?.user?.email}
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Articles</CardTitle>
            <CardDescription>Manage your product catalog</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              Create and manage articles, variants, and pricing.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Prompts</CardTitle>
            <CardDescription>AI prompt management</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              Configure AI prompts for image generation.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Orders</CardTitle>
            <CardDescription>Process customer orders</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              View and manage customer orders and fulfillment.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Users</CardTitle>
            <CardDescription>User management</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              Manage user accounts and permissions.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Analytics</CardTitle>
            <CardDescription>View performance metrics</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              Track sales, user engagement, and system performance.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Settings</CardTitle>
            <CardDescription>System configuration</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">
              Configure system settings and integrations.
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
