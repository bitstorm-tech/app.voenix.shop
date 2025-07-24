# Split the API into a public and secured part

## Requirements
- Split the backend API into a public part and a secured part
- The public part is accessible without any authentication
- The secured part is only accessible when the user is authenticated
- The secured part also has a role system (the roles are `ADMIN` and `USER` for now)
- Refactor the package structure to express the intention of splitting the code into public and secured endpoints 
- Refactor the API endpoint URLs (e.g., admin routes start with `/api/admin`)
- Refactor the Spring security configuration

Think hard to create the best possible architecture and plan.
