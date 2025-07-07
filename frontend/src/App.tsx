import AdminLayout from '@/components/admin/AdminLayout';
import CompletedOrders from '@/pages/admin/CompletedOrders';
import Mugs from '@/pages/admin/Mugs';
import NewPrompt from '@/pages/admin/NewPrompt';
import OpenOrders from '@/pages/admin/OpenOrders';
import PromptCategories from '@/pages/admin/PromptCategories';
import Prompts from '@/pages/admin/Prompts';
import Editor from '@/pages/Editor';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Editor />} />
        <Route path="/editor" element={<Editor />} />

        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Prompts />} />
          <Route path="prompts" element={<Prompts />} />
          <Route path="prompts/new" element={<NewPrompt />} />
          <Route path="prompt-categories" element={<PromptCategories />} />
          <Route path="mugs" element={<Mugs />} />
          <Route
            path="mug-categories"
            element={
              <div className="p-8 pt-20 md:pt-8">
                <h1 className="text-2xl font-semibold">Mug Categories</h1>
                <p className="mt-2 text-gray-600">Mug categories management coming soon...</p>
              </div>
            }
          />
          <Route path="orders/open" element={<OpenOrders />} />
          <Route path="orders/completed" element={<CompletedOrders />} />
          <Route
            path="prompt-tester"
            element={
              <div className="p-8 pt-20 md:pt-8">
                <h1 className="text-2xl font-semibold">Prompt Tester</h1>
                <p className="mt-2 text-gray-600">Prompt testing tool coming soon...</p>
              </div>
            }
          />
        </Route>
      </Routes>
    </Router>
  );
}
