# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Voenix Shop is a full-stack e-commerce application for creating custom mugs with AI-generated images. The codebase is split into two main applications:

- **Backend**: Kotlin Spring Boot REST API (`/backend`) - See `backend/CLAUDE.md` for backend-specific guidance
- **Frontend**: React TypeScript SPA with Vite (`/frontend`) - See `frontend/CLAUDE.md` for frontend-specific guidance

## Common Development Commands

The test credentials to login into the admin site are `a@a` / `test`.

**Note**: For development tasks that involve both frontend and backend changes, consider using specialized subagents (backend-expert and frontend-expert) to work in parallel for maximum efficiency.

## Subagent Usage Guidelines

### When to Use Specialized Subagents

**ALWAYS** consider using specialized subagents for domain-specific tasks:

1. **backend-expert**: Use for all backend-only tasks
   - Creating/modifying Kotlin entities, DTOs, services, controllers
   - Database migrations and repository changes
   - Spring Boot configuration and security updates
   - Backend testing and API endpoint implementation

2. **frontend-expert**: Use for all frontend-only tasks
   - React component creation and updates
   - TypeScript type definitions
   - State management (Zustand stores)
   - UI/UX implementations with Tailwind CSS
   - Frontend routing and form handling

3. **general-purpose**: Use for complex searches and analysis
   - Multi-file searches across the codebase
   - Understanding code relationships and dependencies
   - Investigating bugs that span multiple files

4. **requirements-engineer**: Use for planning and requirements
   - Breaking down complex features into tasks
   - Identifying edge cases and considerations
   - Creating comprehensive requirement documents

### Parallel Execution Examples

**Full-stack feature implementation:**
```
// Launch both agents simultaneously:
Task(description="Backend VAT feature", prompt="...", subagent_type="backend-expert")
Task(description="Frontend VAT feature", prompt="...", subagent_type="frontend-expert")
```

**Complex refactoring:**
```
// Use general-purpose for analysis, then specialized agents for implementation:
Task(description="Find all VAT usages", prompt="...", subagent_type="general-purpose")
// Then launch specialized agents based on findings
```

### Key Principles
- **Think parallel**: Can this task be split between frontend/backend?
- **Use expertise**: Each agent is optimized for their domain
- **Avoid sequential work**: Don't switch between frontend/backend yourself
- **Delegate searches**: Use agents for multi-file searches instead of multiple Grep calls

## Quality Assurance

### Common
- Skeptical mode: question everything, suggest simpler explanations, stay grounded
- **ALWAYS** use specialized subagents for domain-specific tasks (see Subagent Usage Guidelines above)
- Use and spawn subagents to run tasks in parallel whenever possible
- ALWAYS read the latest documentation from context7 mcp server
- Use the puppeteer mcp server to check if the implementation looks right in the browser
- Use `git mv` to move files that are under version control
- Don't write useless, unnecessary or redundant comments -> only use comments to describe complex logic
- Document WHY decisions were made, not just WHAT the code does

## Important Development Notes

1. **Subagent Usage**: ALWAYS use specialized agents for better efficiency:
   - Full-stack features: Launch backend-expert and frontend-expert in parallel
   - Backend-only changes: Use backend-expert agent
   - Frontend-only changes: Use frontend-expert agent
   - Complex searches: Use general-purpose agent
2. **Environment**: Use `.env` files for configuration (supported by spring-dotenv)
3. **API Communication**: Frontend expects backend on http://localhost:8080

## Security Best Practices

- Never store sensitive data in localStorage
- All API calls use authentication headers
- Input validation on both frontend and backend
- Error messages don't expose sensitive information
- Regular security audits and dependency updates

## Development Workflow

### Setting Up Development Environment
1. **Database Setup**:
   - Install PostgreSQL and create database `voenix_java`
   - Database migrations run automatically on backend startup

2. **Backend Setup**:
   ```bash
   cd backend
   ./gradlew build
   ./gradlew bootRun   # Runs on http://localhost:8080
   ```

3. **Frontend Setup**:
   ```bash
   cd frontend
   npm install
   npm run dev         # Runs on http://localhost:3000
   ```

### Working with Admin Features
1. **Create Admin User**: Use the admin API or database seed
2. **Access Admin Panel**: Navigate to `/admin` routes
3. **Test OpenAI Integration**: 
   - Configure OpenAI API key in backend `.env`
   - Use the Prompt Tester at `/admin/prompts/tester`
