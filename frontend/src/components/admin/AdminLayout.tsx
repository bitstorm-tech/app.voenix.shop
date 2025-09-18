import AdminSidebar from '@/components/admin/AdminSidebar';
import { Button } from '@/components/ui/Button';
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle, SheetTrigger } from '@/components/ui/Sheet';
import { Menu } from 'lucide-react';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Outlet } from 'react-router-dom';

interface AdminLayoutProps {
  screenReaderLabels?: {
    navigationDescription?: string;
    navigationTitle?: string;
    toggleMenu?: string;
  };
}

export default function AdminLayout({ screenReaderLabels }: AdminLayoutProps = {}) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { t } = useTranslation('adminLayout');

  const toggleMenuLabel = screenReaderLabels?.toggleMenu ?? t('mobileMenu.toggle');
  const navigationTitle = screenReaderLabels?.navigationTitle ?? t('navigation.title');
  const navigationDescription = screenReaderLabels?.navigationDescription ?? t('navigation.description');

  const handleMobileMenuClose = () => {
    setMobileMenuOpen(false);
  };

  return (
    <div className="flex h-screen">
      {/* Desktop Sidebar */}
      <div className="hidden md:block">
        <AdminSidebar />
      </div>

      {/* Main Content */}
      <div className="relative flex-1 overflow-auto">
        {/* Mobile Menu Button */}
        <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
          <SheetTrigger asChild>
            <Button variant="outline" size="icon" className="absolute top-4 left-4 z-40 bg-white shadow-md md:hidden">
              <Menu className="h-5 w-5" />
              <span className="sr-only">{toggleMenuLabel}</span>
            </Button>
          </SheetTrigger>
          <SheetContent side="left" className="w-64 p-0">
            <SheetHeader className="sr-only">
              <SheetTitle>{navigationTitle}</SheetTitle>
              <SheetDescription>{navigationDescription}</SheetDescription>
            </SheetHeader>
            <AdminSidebar onNavigate={handleMobileMenuClose} />
          </SheetContent>
        </Sheet>

        <Outlet />
      </div>
    </div>
  );
}
