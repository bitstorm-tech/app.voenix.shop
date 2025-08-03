"use client";

import { Button } from "@/components/ui/Button";
import { logoutAction } from "@/app/(auth)/login/actions";

export function LogoutButton({ className }: { className?: string }) {
  return (
    <form action={logoutAction}>
      <Button type="submit" variant="outline" className={className}>
        Log Out
      </Button>
    </form>
  );
}
