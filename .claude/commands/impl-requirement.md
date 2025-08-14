## Multi-Agent Development Workflow

Execute the following phases sequentially. **IMPORTANT: Do not proceed to implementation until receiving explicit user approval of the plan.**

### Phase 1: Requirements Analysis & Architecture Planning
Engage @agent-system-architecture-lead and @agent-requirements-engineer to:
1. Analyze the provided requirements thoroughly
2. Identify technical constraints and dependencies
3. Define system components and their interactions
4. Specify technology stack and architectural patterns
5. Create acceptance criteria for each requirement
6. Estimate implementation complexity and risks
7. Read the documentation either from site or from context7 to understand technologies

**Output Format:**
- Executive summary (2-3 sentences)
- Detailed technical specification
- Component diagram or architecture overview
- Risk assessment with mitigation strategies
- Clear success metrics

### Phase 2: User Review & Approval
1. Present the complete plan to the user
2. Explicitly ask: "Do you approve this plan, or would you like to suggest modifications?"
3. If modifications requested:
    - Document all change requests
    - Update the plan accordingly
    - Re-present for approval
4. Wait for explicit approval (e.g., "yes", "approved", "proceed") before continuing

### Phase 3: Implementation
Upon receiving approval, engage the appropriate development agents:
- For frontend work: @agent-react-frontend-expert
- For backend work: @agent-kotlin-backend-expert
- For full-stack features: Both agents collaborating

Implementation guidelines:
- Follow the approved architectural patterns
- Implement one component at a time
- Include error handling and edge cases
- Write self-documenting code with clear comments
- Create unit tests alongside implementation

### Phase 4: Quality Assurance
Engage @agent-qa-engineer to perform comprehensive review:
1. Code quality metrics (complexity, maintainability, test coverage)
2. Security vulnerability assessment
3. Performance bottleneck identification
4. Best practices compliance check
5. Documentation completeness review

**QA Output Should Include:**
- Pass/Fail status for each quality metric
- Specific issues found with severity levels
- Recommended fixes for any critical issues
- Overall quality score

### Requirements to Analyze:
$ARGUMENTS