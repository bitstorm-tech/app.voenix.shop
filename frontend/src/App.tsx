import AdminLayout from '@/components/admin/AdminLayout';
import ProtectedRoute from '@/components/auth/ProtectedRoute';
import { queryClient } from '@/lib/queryClient';
import ArticleCategories from '@/pages/admin/ArticleCategories';
import Articles from '@/pages/admin/Articles';
import NewOrEditArticle from '@/pages/admin/articles/NewOrEditArticle';
import CompletedOrders from '@/pages/admin/CompletedOrders';
import Logistics from '@/pages/admin/Logistics';
import NewOrEditArticleCategory from '@/pages/admin/NewOrEditArticleCategory';
import NewOrEditArticleSubCategory from '@/pages/admin/NewOrEditArticleSubCategory';
import NewOrEditPrompt from '@/pages/admin/NewOrEditPrompt';
import NewOrEditPromptSlotType from '@/pages/admin/NewOrEditPromptSlotType';
import NewOrEditPromptSlotVariant from '@/pages/admin/NewOrEditPromptSlotVariant';
import NewOrEditSupplier from '@/pages/admin/NewOrEditSupplier';
import NewOrEditVat from '@/pages/admin/NewOrEditVat';
import OpenOrders from '@/pages/admin/OpenOrders';
import PromptCategories from '@/pages/admin/PromptCategories';
import Prompts from '@/pages/admin/Prompts';
import PromptSlotTypes from '@/pages/admin/PromptSlotTypes';
import PromptTester from '@/pages/admin/PromptTester';
import SlotVariants from '@/pages/admin/SlotVariants';
import Suppliers from '@/pages/admin/Suppliers';
import Vat from '@/pages/admin/Vat';
import CartPage from '@/pages/Cart';
import CheckoutPage from '@/pages/Checkout';
import Editor from '@/pages/Editor';
import Login from '@/pages/Login';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { Route, BrowserRouter as Router, Routes } from 'react-router-dom';

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
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
              <Route path="prompt-slot-types" element={<PromptSlotTypes />} />
              <Route path="prompt-slot-types/new" element={<NewOrEditPromptSlotType />} />
              <Route path="prompt-slot-types/:id/edit" element={<NewOrEditPromptSlotType />} />
              <Route path="slot-variants" element={<SlotVariants />} />
              <Route path="slot-variants/new" element={<NewOrEditPromptSlotVariant />} />
              <Route path="slot-variants/:id/edit" element={<NewOrEditPromptSlotVariant />} />
              <Route path="articles" element={<Articles />} />
              <Route path="articles/new" element={<NewOrEditArticle />} />
              <Route path="articles/:id/edit" element={<NewOrEditArticle />} />
              <Route path="article-categories" element={<ArticleCategories />} />
              <Route path="article-categories/new" element={<NewOrEditArticleCategory />} />
              <Route path="article-categories/:id/edit" element={<NewOrEditArticleCategory />} />
              <Route path="article-subcategories/new" element={<NewOrEditArticleSubCategory />} />
              <Route path="article-subcategories/:id/edit" element={<NewOrEditArticleSubCategory />} />
              <Route path="orders/open" element={<OpenOrders />} />
              <Route path="orders/completed" element={<CompletedOrders />} />
              <Route path="suppliers" element={<Suppliers />} />
              <Route path="suppliers/new" element={<NewOrEditSupplier />} />
              <Route path="suppliers/:id/edit" element={<NewOrEditSupplier />} />
              <Route path="logistics" element={<Logistics />} />
              <Route path="vat" element={<Vat />} />
              <Route path="vat/new" element={<NewOrEditVat />} />
              <Route path="vat/:id/edit" element={<NewOrEditVat />} />
              <Route path="prompt-tester" element={<PromptTester />} />
            </Route>
          </Route>
        </Routes>
        <ReactQueryDevtools initialIsOpen={false} />
      </Router>
    </QueryClientProvider>
  );
}
