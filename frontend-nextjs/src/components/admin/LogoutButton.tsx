import { logoutAction } from "@/app/(auth)/login/actions";
import { Button } from "@/components/ui/Button";

export function LogoutButton({ className }: { className?: string }) {
  return (
    <form action={logoutAction}>
      <Button type="submit" variant="outline" className={className}>
        Log Out
      </Button>
    </form>
  );
}
