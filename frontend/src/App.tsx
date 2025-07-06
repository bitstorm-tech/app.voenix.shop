import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom'
import Home from './pages/Home'
import About from './pages/About'
import './App.css'
import Editor from '@/pages/Editor';

function App() {
  return (
    <Router>
      <div className="app">
        <header className="app-header">
          <h1>Voenix Shop</h1>
          <nav>
            <Link to="/">Home</Link>
            <Link to="/about">About</Link>
          </nav>
        </header>
        
        <main className="app-main">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/editor" element={<Editor />} />
          </Routes>
        </main>
        
        <footer className="app-footer">
          <p>&copy; 2025 Voenix Shop. All rights reserved.</p>
        </footer>
      </div>
    </Router>
  )
}

export default App