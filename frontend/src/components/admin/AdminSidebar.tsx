import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/Accordion';
import { Button } from '@/components/ui/Button';
import { useLogout, useSession } from '@/hooks/queries/useAuth';
import { Box, Database, FileText, FlaskConical, LogOut, Palette, ShoppingBag } from 'lucide-react';
import { useState } from 'react';
import { NavLink } from 'react-router-dom';

interface AdminSidebarProps {
  user?: { name?: string };
  onNavigate?: () => void;
}

export default function AdminSidebar({ onNavigate }: AdminSidebarProps = {}) {
  const [openItems, setOpenItems] = useState<string[]>(['prompts', 'articles', 'orders', 'masterdata']);
  const logoutMutation = useLogout();
  const { data: session } = useSession();
  const user = session?.user;

  const handleLogout = () => {
    logoutMutation.mutate();
  };

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
              <NavLink
                to="/admin/prompts"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                All Prompts
              </NavLink>
              <NavLink
                to="/admin/prompt-categories"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Categories
              </NavLink>
              <NavLink
                to="/admin/prompt-slot-types"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Slot Types
              </NavLink>
              <NavLink
                to="/admin/slot-variants"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Slot Variants
              </NavLink>
            </AccordionContent>
          </AccordionItem>

          {/* Articles Section */}
          <AccordionItem value="articles" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <Box className="h-4 w-4" />
                <span>Articles</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <NavLink
                to="/admin/articles"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                All Articles
              </NavLink>
              <NavLink
                to="/admin/article-categories"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Categories
              </NavLink>
            </AccordionContent>
          </AccordionItem>

          {/* Masterdata Section */}
          <AccordionItem value="masterdata" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <Database className="h-4 w-4" />
                <span>Masterdata</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <NavLink
                to="/admin/suppliers"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Supplier
              </NavLink>
              <NavLink
                to="/admin/logistics"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Logistics
              </NavLink>
              <NavLink
                to="/admin/vat"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                VAT
              </NavLink>
            </AccordionContent>
          </AccordionItem>

          {/* Orders Section */}
          <AccordionItem value="orders" className="border-none">
            <AccordionTrigger className="rounded-md px-3 py-2 hover:bg-gray-100 hover:no-underline">
              <div className="flex items-center gap-2">
                <ShoppingBag className="h-4 w-4" />
                <span>Orders</span>
              </div>
            </AccordionTrigger>
            <AccordionContent className="ml-4 space-y-1">
              <NavLink
                to="/admin/orders/open"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Open Orders
              </NavLink>
              <NavLink
                to="/admin/orders/completed"
                onClick={onNavigate}
                className={({ isActive }) =>
                  `flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm transition-colors hover:bg-gray-100 ${
                    isActive ? 'bg-gray-200 font-medium' : ''
                  }`
                }
              >
                Completed Orders
              </NavLink>
            </AccordionContent>
          </AccordionItem>
        </Accordion>

        {/* Prompt Tester - Outside accordion */}
        <div className="mt-4">
          <NavLink
            to="/admin/prompt-tester"
            onClick={onNavigate}
            className={({ isActive }) =>
              `flex w-full items-center gap-2 rounded-md px-3 py-2 transition-colors hover:bg-gray-100 ${isActive ? 'bg-gray-200 font-medium' : ''}`
            }
          >
            <FlaskConical className="h-4 w-4" />
            <span>Prompt Tester</span>
          </NavLink>
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
          <p className="text-sm text-gray-600">{user?.firstName || user?.email || 'Admin'}</p>
        </div>
        <Button variant="outline" className="w-full justify-start" onClick={handleLogout}>
          <LogOut className="mr-2 h-4 w-4" />
          Logout
        </Button>
      </div>
    </aside>
  );
}
