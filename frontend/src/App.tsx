import AdminLayout from '@/components/admin/AdminLayout';
import CompletedOrders from '@/pages/admin/CompletedOrders';
import MugCategories from '@/pages/admin/MugCategories';
import Mugs from '@/pages/admin/Mugs';
import NewOrEditMug from '@/pages/admin/NewOrEditMug';
import NewOrEditMugCategory from '@/pages/admin/NewOrEditMugCategory';
import NewOrEditMugSubCategory from '@/pages/admin/NewOrEditMugSubCategory';
import NewOrEditPrompt from '@/pages/admin/NewOrEditPrompt';
import NewOrEditPromptCategory from '@/pages/admin/NewOrEditPromptCategory';
import NewOrEditPromptSubCategory from '@/pages/admin/NewOrEditPromptSubCategory';
import NewOrEditSlot from '@/pages/admin/NewOrEditSlot';
import NewOrEditSlotType from '@/pages/admin/NewOrEditSlotType';
import OpenOrders from '@/pages/admin/OpenOrders';
import PromptCategories from '@/pages/admin/PromptCategories';
import Prompts from '@/pages/admin/Prompts';
import PromptSubCategories from '@/pages/admin/PromptSubCategories';
import Slots from '@/pages/admin/Slots';
import SlotTypes from '@/pages/admin/SlotTypes';
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
          <Route path="prompts/new" element={<NewOrEditPrompt />} />
          <Route path="prompts/:id/edit" element={<NewOrEditPrompt />} />
          <Route path="prompt-categories" element={<PromptCategories />} />
          <Route path="prompt-categories/new" element={<NewOrEditPromptCategory />} />
          <Route path="prompt-categories/:id/edit" element={<NewOrEditPromptCategory />} />
          <Route path="prompt-subcategories" element={<PromptSubCategories />} />
          <Route path="prompt-subcategories/new" element={<NewOrEditPromptSubCategory />} />
          <Route path="prompt-subcategories/:id/edit" element={<NewOrEditPromptSubCategory />} />
          <Route path="slot-types" element={<SlotTypes />} />
          <Route path="slot-types/new" element={<NewOrEditSlotType />} />
          <Route path="slot-types/:id/edit" element={<NewOrEditSlotType />} />
          <Route path="slots" element={<Slots />} />
          <Route path="slots/new" element={<NewOrEditSlot />} />
          <Route path="slots/:id/edit" element={<NewOrEditSlot />} />
          <Route path="mugs" element={<Mugs />} />
          <Route path="mugs/new" element={<NewOrEditMug />} />
          <Route path="mugs/:id/edit" element={<NewOrEditMug />} />
          <Route path="mug-categories" element={<MugCategories />} />
          <Route path="mug-categories/new" element={<NewOrEditMugCategory />} />
          <Route path="mug-categories/:id/edit" element={<NewOrEditMugCategory />} />
          <Route path="mug-subcategories/new" element={<NewOrEditMugSubCategory />} />
          <Route path="mug-subcategories/:id/edit" element={<NewOrEditMugSubCategory />} />
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
