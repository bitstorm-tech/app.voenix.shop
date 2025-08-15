import { lazy } from 'react';
import { Route, Routes } from 'react-router-dom';

// Lazy load all admin components
const AdminLayout = lazy(() => import('@/components/admin/AdminLayout'));
const ArticleCategories = lazy(() => import('@/pages/admin/ArticleCategories'));
const Articles = lazy(() => import('@/pages/admin/Articles'));
const NewOrEditArticle = lazy(() => import('@/pages/admin/articles/NewOrEditArticle'));
const CopyVariants = lazy(() => import('@/pages/admin/articles/CopyVariants'));
const CompletedOrders = lazy(() => import('@/pages/admin/CompletedOrders'));
const Logistics = lazy(() => import('@/pages/admin/Logistics'));
const NewOrEditArticleCategory = lazy(() => import('@/pages/admin/NewOrEditArticleCategory'));
const NewOrEditArticleSubCategory = lazy(() => import('@/pages/admin/NewOrEditArticleSubCategory'));
const NewOrEditPrompt = lazy(() => import('@/pages/admin/NewOrEditPrompt'));
const NewOrEditPromptSlotType = lazy(() => import('@/pages/admin/NewOrEditPromptSlotType'));
const NewOrEditPromptSlotVariant = lazy(() => import('@/pages/admin/NewOrEditPromptSlotVariant'));
const NewOrEditSupplier = lazy(() => import('@/pages/admin/NewOrEditSupplier'));
const NewOrEditVat = lazy(() => import('@/pages/admin/NewOrEditVat'));
const OpenOrders = lazy(() => import('@/pages/admin/OpenOrders'));
const PromptCategories = lazy(() => import('@/pages/admin/PromptCategories'));
const Prompts = lazy(() => import('@/pages/admin/Prompts'));
const PromptSlotTypes = lazy(() => import('@/pages/admin/PromptSlotTypes'));
const PromptTester = lazy(() => import('@/pages/admin/PromptTester'));
const SlotVariants = lazy(() => import('@/pages/admin/SlotVariants'));
const Suppliers = lazy(() => import('@/pages/admin/Suppliers'));
const Vat = lazy(() => import('@/pages/admin/Vat'));

export function AdminRoutes() {
  return (
    <Routes>
      <Route element={<AdminLayout />}>
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
        <Route path="articles/:id/copy-variants" element={<CopyVariants />} />
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
    </Routes>
  );
}
