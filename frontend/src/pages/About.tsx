function About() {
  return (
    <div>
      <h2>About Voenix Shop</h2>
      <p>This is a modern React application template featuring:</p>
      <div style={{ marginTop: '1rem' }}>
        <h3>Technology Stack</h3>
        <ul>
          <li><strong>React 19</strong> - The latest version with improved performance</li>
          <li><strong>TypeScript</strong> - For type-safe development</li>
          <li><strong>Vite</strong> - Lightning-fast build tool</li>
          <li><strong>React Router</strong> - For client-side routing</li>
          <li><strong>Bun</strong> - Fast JavaScript runtime and package manager</li>
        </ul>
      </div>
      <div style={{ marginTop: '1rem' }}>
        <h3>Getting Started</h3>
        <p>To start the development server, run:</p>
        <code style={{ 
          backgroundColor: '#f4f4f4', 
          padding: '0.5rem 1rem', 
          borderRadius: '4px',
          display: 'inline-block',
          marginTop: '0.5rem'
        }}>
          bun run dev
        </code>
      </div>
    </div>
  )
}

export default About