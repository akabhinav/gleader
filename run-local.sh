#!/bin/bash

# Gaming Leaderboard - Local Run Script
# This script starts Redis and the application with one command

set -e

echo "🎮 Gaming Leaderboard - Starting Local Development Environment"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "❌ Docker Compose not found. Please install Docker Compose first."
    exit 1
fi

# Use docker-compose or docker compose based on availability
DOCKER_COMPOSE_CMD="docker-compose"
if ! command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
fi

echo "📦 Step 1: Starting Redis..."
$DOCKER_COMPOSE_CMD -f docker-compose.local.yml up -d

# Wait for Redis to be ready
echo "⏳ Waiting for Redis to start..."
for i in {1..30}; do
    if docker exec leaderboard-redis redis-cli ping &> /dev/null; then
        echo "✅ Redis is ready!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Redis failed to start"
        exit 1
    fi
    sleep 1
done

echo ""
echo "🚀 Step 2: Starting Spring Boot Application..."
echo ""

# Run the application
./mvnw spring-boot:run

# Note: When you stop the app (Ctrl+C), Redis will keep running
# To stop Redis, run: docker-compose -f docker-compose.local.yml down
