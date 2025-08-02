import { requireAdmin } from "@/lib/auth/server";
import { AdminSidebar } from "@/components/admin/AdminSidebar";

// Force dynamic rendering since we use cookies
export const dynamic = "force-dynamic";

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // Require admin access for all admin routes
  await requireAdmin();

  return (
    <div className="flex h-screen bg-gray-100">
      <AdminSidebar />
      <main className="flex-1 overflow-auto">{children}</main>
    </div>
  );
}
