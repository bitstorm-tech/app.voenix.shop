// Use revalidate for better performance
export const revalidate = 0;

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  // Let middleware handle all authentication redirects
  // to prevent redirect loops between middleware and layout
  return <div className="min-h-screen bg-gray-50">{children}</div>;
}
