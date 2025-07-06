import { BrowserRouter as Router, Routes, Route, } from 'react-router-dom'
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

            {/* 3. Shop Group Routes (uses ShopLayout with MenuTwo) */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route path="prompts" element={<AdminPrompts />} />
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
