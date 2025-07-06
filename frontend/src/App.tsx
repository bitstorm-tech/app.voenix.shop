import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Editor from '@/pages/Editor';
import AdminLayout from '@/components/admin/AdminLayout';
import AdminPrompts from '@/components/admin/AdminPrompts';

export default function App() {
  return (
    <Router>
      <div className="app">
        {/*<header className="app-header">*/}
        {/*  <h1>Voenix Shop</h1>*/}
        {/*  <nav>*/}
        {/*    <Link to="/">Home</Link>*/}
        {/*    <Link to="/about">About</Link>*/}
        {/*  </nav>*/}
        {/*</header>*/}
        
        <main className="app-main">
          <Routes>
            <Route path="/" element={<Editor />} />
            <Route path="/editor" element={<Editor />} />

            {/* Admin Routes */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route path="prompts" element={<AdminPrompts />} />
              <Route path="prompt-categories" element={<div className="p-8"><h1 className="text-2xl font-semibold">Prompt Categories</h1><p className="mt-2 text-gray-600">Prompt categories management coming soon...</p></div>} />
              <Route path="mugs" element={<div className="p-8"><h1 className="text-2xl font-semibold">All Mugs</h1><p className="mt-2 text-gray-600">Mug management coming soon...</p></div>} />
              <Route path="mug-categories" element={<div className="p-8"><h1 className="text-2xl font-semibold">Mug Categories</h1><p className="mt-2 text-gray-600">Mug categories management coming soon...</p></div>} />
              <Route path="orders/open" element={<div className="p-8"><h1 className="text-2xl font-semibold">Open Orders</h1><p className="mt-2 text-gray-600">Open orders management coming soon...</p></div>} />
              <Route path="orders/completed" element={<div className="p-8"><h1 className="text-2xl font-semibold">Completed Orders</h1><p className="mt-2 text-gray-600">Completed orders management coming soon...</p></div>} />
              <Route path="prompt-tester" element={<div className="p-8"><h1 className="text-2xl font-semibold">Prompt Tester</h1><p className="mt-2 text-gray-600">Prompt testing tool coming soon...</p></div>} />
            </Route>
          </Routes>
        </main>
        
        {/*<footer className="app-footer">*/}
        {/*  <p>&copy; 2025 Voenix Shop. All rights reserved.</p>*/}
        {/*</footer>*/}
      </div>
    </Router>
  )
}
