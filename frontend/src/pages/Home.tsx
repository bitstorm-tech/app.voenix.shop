import { useState } from 'react'

function Home() {
  const [count, setCount] = useState(0)

  return (
    <div>
      <h2>Welcome to Voenix Shop</h2>
      <p>This is a simple React application built with:</p>
      <ul>
        <li>React 19</li>
        <li>TypeScript</li>
        <li>Vite</li>
        <li>React Router</li>
        <li>Bun</li>
      </ul>
      
      <div style={{ marginTop: '2rem' }}>
        <h3>Simple Counter Demo</h3>
        <p>Count: {count}</p>
        <button 
          onClick={() => setCount(count + 1)}
          style={{
            padding: '0.5rem 1rem',
            fontSize: '1rem',
            backgroundColor: '#0066cc',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Increment
        </button>
      </div>
    </div>
  )
}

export default Home