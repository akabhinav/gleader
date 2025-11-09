# Quick Start Guide 🚀

## Run Locally in 3 Ways

### Option 1: Local with Redis (Recommended) ⚡

**Best performance with Redis-powered O(log n) rankings**

```bash
# 1. Start Redis with Docker
docker-compose -f docker-compose.local.yml up -d

# 2. Run the application
./mvnw spring-boot:run

# 3. Access the application
open http://localhost:8080
```

**What you get:**
- ✅ 1000x faster rank calculations
- ✅ Sub-10ms response times
- ✅ Scales to 100M+ players
- ✅ H2 in-memory database (no PostgreSQL needed)

---

### Option 2: Local without Redis (Simple)

**Quick start without any dependencies**

```bash
# 1. Disable Redis
export REDIS_ENABLED=false

# 2. Run the application
./mvnw spring-boot:run

# 3. Access the application
open http://localhost:8080
```

**What you get:**
- ✅ No dependencies needed
- ✅ Works immediately
- ⚠️ Database-based rankings (slower)
- ⚠️ Good for <100K players

---

### Option 3: Full Production Stack

**Complete setup with PostgreSQL + Redis**

```bash
# 1. Start all services
docker-compose up -d

# 2. Check status
docker-compose ps
docker-compose logs -f leaderboard-app

# 3. Access the application
open http://localhost:8080
```

**What you get:**
- ✅ Production-ready PostgreSQL
- ✅ Redis for performance
- ✅ All services containerized
- ✅ Persistent data storage

---

## Testing the API

### 1. Check Status
```bash
curl http://localhost:8080/api/v1/leaderboard/status
```

Response shows if Redis is active:
```json
{
  "service": "Leaderboard",
  "mode": "Redis (active)",
  "redisAvailable": true
}
```

### 2. Create a Player
```bash
curl -X POST http://localhost:8080/api/v1/players \
  -H "Content-Type: application/json" \
  -d '{
    "username": "speedking",
    "email": "speedking@example.com",
    "displayName": "Speed King"
  }'
```

### 3. Submit a Score
```bash
curl -X POST http://localhost:8080/api/v1/scores \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": 1,
    "scoreValue": 25000,
    "gameType": "RACING",
    "levelReached": 50
  }'
```

### 4. Get Leaderboard
```bash
# Top 10 players in RACING
curl http://localhost:8080/api/v1/leaderboard/RACING?page=0&size=10

# Weekly leaderboard
curl http://localhost:8080/api/v1/leaderboard/RACING/period/WEEKLY?size=10

# Get player's rank
curl http://localhost:8080/api/v1/leaderboard/RACING/rank/player/1

# Get leaderboard around player
curl http://localhost:8080/api/v1/leaderboard/RACING/around/player/1?contextSize=5
```

---

## URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **API** | http://localhost:8080 | - |
| **H2 Console** | http://localhost:8080/h2-console | JDBC URL: `jdbc:h2:mem:leaderboard` <br> Username: `sa` <br> Password: (empty) |
| **Redis CLI** | `docker exec -it leaderboard-redis redis-cli` | - |

---

## Verify Redis is Working

### Check Redis Connection
```bash
# If using docker-compose.local.yml
docker exec -it leaderboard-redis redis-cli ping
# Should return: PONG

# Check leaderboard data
docker exec -it leaderboard-redis redis-cli ZRANGE "leaderboard:racing" 0 10 WITHSCORES
```

### Monitor Redis in Real-time
```bash
# Watch Redis commands
docker exec -it leaderboard-redis redis-cli MONITOR
```

### Check Application Logs
```bash
# Look for these log messages
./mvnw spring-boot:run | grep -i redis
```

You should see:
```
Redis leaderboard service enabled
Redis service injected into ScoreService
Using Redis for leaderboard query: RACING
```

---

## Performance Testing

### Load Test with sample data

```bash
# Create 100 players and submit 1000 scores
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/v1/players \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"player$i\",
      \"email\": \"player$i@example.com\",
      \"displayName\": \"Player $i\"
    }" -s > /dev/null
done

# Submit random scores
for i in {1..1000}; do
  PLAYER_ID=$((1 + $RANDOM % 100))
  SCORE=$((1000 + $RANDOM % 50000))
  curl -X POST http://localhost:8080/api/v1/scores \
    -H "Content-Type: application/json" \
    -d "{
      \"playerId\": $PLAYER_ID,
      \"scoreValue\": $SCORE,
      \"gameType\": \"RACING\"
    }" -s > /dev/null

  if [ $((i % 100)) -eq 0 ]; then
    echo "Submitted $i scores..."
  fi
done

echo "✅ Test data created!"
```

### Benchmark Response Times

```bash
# Test leaderboard query (10 times)
echo "Testing leaderboard query..."
for i in {1..10}; do
  curl -w "\nTime: %{time_total}s\n" -o /dev/null -s \
    http://localhost:8080/api/v1/leaderboard/RACING?size=100
done

# Test rank query (10 times)
echo "\nTesting rank query..."
for i in {1..10}; do
  curl -w "\nTime: %{time_total}s\n" -o /dev/null -s \
    http://localhost:8080/api/v1/leaderboard/RACING/rank/player/1
done
```

**Expected Results:**
- **With Redis**: 0.005 - 0.015 seconds (5-15ms)
- **Without Redis**: 0.050 - 0.500 seconds (50-500ms)

---

## Troubleshooting

### Redis connection failed
```bash
# Check if Redis is running
docker ps | grep redis

# Check Redis logs
docker logs leaderboard-redis

# Restart Redis
docker-compose -f docker-compose.local.yml restart redis
```

### Application won't start with Redis
```bash
# Disable Redis and run with database only
export REDIS_ENABLED=false
./mvnw spring-boot:run
```

### Port already in use
```bash
# Check what's using port 8080
lsof -i :8080

# Or change the port
./mvnw spring-boot:run -Dserver.port=8081
```

### H2 Console not accessible
```bash
# Make sure application is running
curl http://localhost:8080/actuator/health

# H2 Console URL
open http://localhost:8080/h2-console
```

---

## Stopping Services

```bash
# Stop Redis only
docker-compose -f docker-compose.local.yml down

# Stop full stack
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

---

## Development Tips

### Hot Reload
```bash
# Use Spring DevTools for auto-restart
./mvnw spring-boot:run
```

### Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=LeaderboardServiceTest
```

### View Database
```bash
# H2 Console
open http://localhost:8080/h2-console

# Settings:
JDBC URL: jdbc:h2:mem:leaderboard
Username: sa
Password: (leave empty)
```

### Redis Data Inspection
```bash
# Connect to Redis
docker exec -it leaderboard-redis redis-cli

# Common commands
KEYS *                              # List all keys
ZRANGE leaderboard:racing 0 -1     # Get all RACING scores
ZCARD leaderboard:racing           # Count entries
ZREVRANGE leaderboard:racing 0 9   # Get top 10
FLUSHALL                           # Clear all data
```

---

## Next Steps

1. ✅ **Explore the API**: Check out the full API docs in [README.md](README.md)
2. ✅ **Review Architecture**: See [SCALABILITY_IMPROVEMENTS.md](SCALABILITY_IMPROVEMENTS.md)
3. ✅ **Compare Performance**: Read [PERFORMANCE_COMPARISON.md](PERFORMANCE_COMPARISON.md)
4. ✅ **Deploy**: Use Docker Compose for production deployment

---

**Need Help?** Check the main [README.md](README.md) for comprehensive documentation.
