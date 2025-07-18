#!/bin/bash

echo "===== Testing Authentication Configuration ====="
echo

# Test public endpoints (should return 200 or proper response)
echo "1. Testing public endpoints (should be accessible without auth):"
echo "   - GET /api/prompts"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/prompts
echo

echo "   - GET /api/mugs"  
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/mugs
echo

echo "   - GET /api/slots"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/slots
echo

echo "   - POST /api/auth/login (should be accessible)"
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"test@test.com","password":"test"}'
echo -e "\n"

# Test authenticated endpoints (should return 401)
echo "2. Testing authenticated endpoints without auth (should return 401):"
echo "   - GET /api/auth/session"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/auth/session
echo

echo "   - POST /api/auth/logout"
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/auth/logout
echo -e "\n"

# Test admin endpoints (should return 401)
echo "3. Testing admin endpoints without auth (should return 401):"
echo "   - GET /api/users"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/users
echo

echo "   - POST /api/prompts"
curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/prompts -H "Content-Type: application/json" -d '{}'
echo

echo "   - DELETE /api/mugs/1"
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/mugs/1
echo -e "\n"

# Test undefined endpoints (should return 401 - deny by default)
echo "4. Testing undefined endpoints (should return 401 - deny by default):"
echo "   - GET /api/undefined-endpoint"
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/undefined-endpoint
echo -e "\n"

echo "===== Test Complete ====="