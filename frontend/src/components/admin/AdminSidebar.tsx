import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Button } from '@/components/ui/Button';
import { Coffee, FileText, FlaskConical, LogOut, Package, Palette } from 'lucide-react';
import { useState } from 'react';

interface AdminSidebarProps {
  onNavigate?: () => void;
}

export default function AdminSidebar({ onNavigate }: AdminSidebarProps) {
  const [openItems, setOpenItems] = useState<string[]>(['prompts', 'mugs', 'orders']);

  const handleLogout = () => {
    // router.post('/logout');
  };

  const handleNavigate = (_route: string) => {
    // router.visit(route);
    if (onNavigate) {
      onNavigate();
    }
  };

  const isActive = (_path: string) => false; //url === path;

  return (
    <aside className="flex h-full w-64 flex-col border-r bg-gray-50 md:h-screen">
      <div className="flex h-16 items-center border-b px-6">
        <h2 className="text-xl font-semibold">Admin Panel</h2>
      </div>

      <div className="flex-1 overflow-y-auto p-4">
        <Accordion type="multiple" value={openItems} onValueChange={setOpenItems} className="space-y-2">
          {/* Prompts Section */}
          <AccordionItem value="prompts" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <FileText className="h-4 w-4" />
                <span>Prompts</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <button
                onClick={() => handleNavigate('/admin/prompts')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/prompts') || isActive('/admin') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                All Prompts
              </button>
              <button
                onClick={() => handleNavigate('/admin/prompt-categories')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/prompt-categories') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                Categories
              </button>
            </AccordionContent>
          </AccordionItem>

          {/* Mugs Section */}
          <AccordionItem value="mugs" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <Coffee className="h-4 w-4" />
                <span>Mugs</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <button
                onClick={() => handleNavigate('/admin/mugs')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/mugs') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                All Mugs
              </button>
              <button
                onClick={() => handleNavigate('/admin/mug-categories')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/mug-categories') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                Categories
              </button>
            </AccordionContent>
          </AccordionItem>

          {/* Orders Section */}
          <AccordionItem value="orders" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <Package className="h-4 w-4" />
                <span>Orders</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <button
                onClick={() => handleNavigate('/admin/orders/open')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/orders/open') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                Open Orders
              </button>
              <button
                onClick={() => handleNavigate('/admin/orders/completed')}
                className={`flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                  isActive('/admin/orders/completed') ? 'bg-gray-200 font-medium' : ''
                }`}
              >
                Completed Orders
              </button>
            </AccordionContent>
          </AccordionItem>
        </Accordion>

        {/* Prompt Tester - Outside accordion */}
        <div className="mt-4">
          <button
            onClick={() => handleNavigate('/admin/prompt-tester')}
            className={`flex w-full items-center gap-2 rounded-md px-3 py-2 transition-colors hover:bg-gray-100 ${
              isActive('/admin/prompt-tester') ? 'bg-gray-200 font-medium' : ''
            }`}
          >
            <FlaskConical className="h-4 w-4" />
            <span>Prompt Tester</span>
          </button>
        </div>

        {/* Editor - Opens in new tab */}
        <div className="mt-2">
          <button
            onClick={() => window.open('/editor', '_blank')}
            className="flex w-full items-center gap-2 rounded-md px-3 py-2 transition-colors hover:bg-gray-100"
          >
            <Palette className="h-4 w-4" />
            <span>Editor</span>
          </button>
        </div>
      </div>

      <div className="border-t p-4">
        <div className="mb-3 px-2">
          <p className="text-sm text-gray-600">DuDa</p>
        </div>
        <Button variant="outline" className="w-full justify-start" onClick={handleLogout}>
          <LogOut className="mr-2 h-4 w-4" />
          Logout
        </Button>
      </div>
    </aside>
  );
}
