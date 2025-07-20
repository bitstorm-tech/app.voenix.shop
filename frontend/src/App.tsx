import AdminLayout from '@/components/admin/AdminLayout';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import ArticleCategories from '@/pages/admin/ArticleCategories';
import CompletedOrders from '@/pages/admin/CompletedOrders';
import Mugs from '@/pages/admin/Mugs';
import NewOrEditMug from '@/pages/admin/mugs/NewOrEditMug';
import NewOrEditArticleCategory from '@/pages/admin/NewOrEditArticleCategory';
import NewOrEditArticleSubCategory from '@/pages/admin/NewOrEditArticleSubCategory';
import NewOrEditPrompt from '@/pages/admin/NewOrEditPrompt';
import NewOrEditSlot from '@/pages/admin/NewOrEditSlot';
import NewOrEditSlotType from '@/pages/admin/NewOrEditSlotType';
import OpenOrders from '@/pages/admin/OpenOrders';
import PromptCategories from '@/pages/admin/PromptCategories';
import Prompts from '@/pages/admin/Prompts';
import Slots from '@/pages/admin/Slots';
import SlotTypes from '@/pages/admin/SlotTypes';
import CartPage from '@/pages/Cart';
import CheckoutPage from '@/pages/Checkout';
import Editor from '@/pages/Editor';
import Login from '@/pages/Login';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';

export default function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Editor />} />
        <Route path="/editor" element={<Editor />} />
        <Route path="/cart" element={<CartPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/login" element={<Login />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<Prompts />} />
            <Route path="prompts" element={<Prompts />} />
            <Route path="prompts/new" element={<NewOrEditPrompt />} />
            <Route path="prompts/:id/edit" element={<NewOrEditPrompt />} />
            <Route path="prompt-categories" element={<PromptCategories />} />
            <Route path="slot-types" element={<SlotTypes />} />
            <Route path="slot-types/new" element={<NewOrEditSlotType />} />
            <Route path="slot-types/:id/edit" element={<NewOrEditSlotType />} />
            <Route path="slots" element={<Slots />} />
            <Route path="slots/new" element={<NewOrEditSlot />} />
            <Route path="slots/:id/edit" element={<NewOrEditSlot />} />
            <Route path="mugs" element={<Mugs />} />
            <Route path="mugs/new" element={<NewOrEditMug />} />
            <Route path="mugs/:id/edit" element={<NewOrEditMug />} />
            <Route path="article-categories" element={<ArticleCategories />} />
            <Route path="article-categories/new" element={<NewOrEditArticleCategory />} />
            <Route path="article-categories/:id/edit" element={<NewOrEditArticleCategory />} />
            <Route path="article-subcategories/new" element={<NewOrEditArticleSubCategory />} />
            <Route path="article-subcategories/:id/edit" element={<NewOrEditArticleSubCategory />} />
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
        </Route>
      </Routes>
    </Router>
  );
}
