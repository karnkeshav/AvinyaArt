#!/bin/bash

# Build and start backend
cd backend
docker-compose up --build -d

echo "Backend running on http://localhost:8000"
echo "To stop: docker-compose down"
