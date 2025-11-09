#!/bin/bash

# Gaming Leaderboard - Stop Local Services

echo "🛑 Stopping Gaming Leaderboard Services..."

# Use docker-compose or docker compose based on availability
DOCKER_COMPOSE_CMD="docker-compose"
if ! command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_CMD="docker compose"
fi

# Stop Redis
$DOCKER_COMPOSE_CMD -f docker-compose.local.yml down

echo "✅ All services stopped"
echo ""
echo "To start again, run: ./run-local.sh"
