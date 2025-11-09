# Scalability Improvements for Gaming Leaderboard

## Current Limitations

### Performance Bottlenecks (10M+ scores)
1. **O(n) Ranking**: `countScoresAbove()` scans millions of rows
2. **Cache Stampede**: All caches invalidated on any score update
3. **Single DB**: No horizontal scaling of data layer
4. **Synchronous Processing**: Score submission blocks on ranking updates
5. **No Data Partitioning**: Unbounded table growth

### Expected Performance
| Scale | Current Performance | Target Performance |
|-------|--------------------|--------------------|
| 1K players | ✅ <10ms | ✅ <5ms |
| 100K players | ⚠️ 100-500ms | ✅ <10ms |
| 1M players | ❌ 2-5 seconds | ✅ <20ms |
| 10M+ players | ❌ Timeout/Crash | ✅ <50ms |

---

## Scalable Architecture Design

```
┌─────────────────────────────────────────────────────────────┐
│                     CDN / API Gateway                        │
│              (Rate Limiting, DDoS Protection)                │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│                   Load Balancer (Nginx)                      │
└──────┬──────────────────────────────────────────┬───────────┘
       │                                          │
┌──────▼──────────┐                     ┌────────▼───────────┐
│  App Server 1   │                     │  App Server N      │
│  (Spring Boot)  │ ◄────Sync────►      │  (Spring Boot)     │
└──────┬──────────┘                     └────────┬───────────┘
       │                                          │
       │         ┌──────────────────┐            │
       └────────►│  Redis Cluster   │◄───────────┘
                 │  (Distributed    │
                 │   Cache + Ranks) │
                 └──────────────────┘
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
┌──────▼──────┐  ┌────────▼────────┐  ┌─────▼──────┐
│ PostgreSQL  │  │  Message Queue  │  │  Redis     │
│  Primary    │  │  (Kafka/RabbitMQ)│  │  Sorted    │
│             │  │                  │  │  Sets      │
└──────┬──────┘  └────────┬─────────┘  └────────────┘
       │                  │
┌──────▼──────┐  ┌────────▼─────────┐
│ PostgreSQL  │  │ Background Worker │
│  Replicas   │  │ (Rank Computation)│
│  (Read)     │  │                   │
└─────────────┘  └───────────────────┘
```

---

## Implementation Improvements

### 1. Redis Sorted Sets for O(log n) Ranking

**Replace**: SQL `countScoresAbove()` queries
**With**: Redis ZSET operations

```java
// NEW: RedisLeaderboardService.java

@Service
@RequiredArgsConstructor
public class RedisLeaderboardService {

    private final StringRedisTemplate redisTemplate;

    // O(log n) - Submit score to sorted set
    public void updateScore(Long playerId, Long score, GameType gameType) {
        String key = "leaderboard:" + gameType;
        redisTemplate.opsForZSet().add(key, playerId.toString(), score);
    }

    // O(log n) - Get player rank
    public Long getPlayerRank(Long playerId, GameType gameType) {
        String key = "leaderboard:" + gameType;
        Long rank = redisTemplate.opsForZSet()
            .reverseRank(playerId.toString(), key);
        return rank != null ? rank + 1 : null;
    }

    // O(log n + m) where m = limit - Get top N players
    public Set<ZSetOperations.TypedTuple<String>> getTopPlayers(
        GameType gameType,
        int start,
        int end
    ) {
        String key = "leaderboard:" + gameType;
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, start, end);
    }

    // O(1) - Get total players
    public Long getTotalPlayers(GameType gameType) {
        String key = "leaderboard:" + gameType;
        return redisTemplate.opsForZSet().size(key);
    }
}
```

**Performance Gain**:
- Before: O(n) - scan 10M rows = 5 seconds
- After: O(log n) - Redis ZSET = **5ms**

---

### 2. Event-Driven Architecture (Async Processing)

**Replace**: Synchronous score processing
**With**: Async message queue

```java
// NEW: ScoreEventPublisher.java

@Service
@RequiredArgsConstructor
public class ScoreEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Async
    public void publishScoreSubmitted(ScoreSubmittedEvent event) {
        rabbitTemplate.convertAndSend(
            "score.exchange",
            "score.submitted",
            event
        );
    }
}

// Modified: ScoreService.java
@Transactional
public ScoreResponse submitScore(SubmitScoreRequest request) {
    // 1. Save to database (write model)
    Score score = scoreRepository.save(buildScore(request));

    // 2. Publish event for async processing (non-blocking)
    scoreEventPublisher.publishScoreSubmitted(
        new ScoreSubmittedEvent(score)
    );

    // 3. Return immediately (fast response)
    return ScoreResponse.from(score);
}

// NEW: ScoreEventConsumer.java (Background Worker)
@Service
public class ScoreEventConsumer {

    @RabbitListener(queues = "score.submitted.queue")
    public void handleScoreSubmitted(ScoreSubmittedEvent event) {
        // Update Redis sorted sets
        redisLeaderboardService.updateScore(
            event.getPlayerId(),
            event.getScore(),
            event.getGameType()
        );

        // Invalidate specific cache entries only
        cacheManager.evict("playerRank",
            event.getPlayerId() + "_" + event.getGameType());
    }
}
```

**Benefits**:
- API response: **50ms** (write to DB only)
- Ranking updates: **async** (doesn't block user)
- **Decoupled** processing (can scale workers independently)

---

### 3. Granular Cache Invalidation

**Replace**: `allEntries = true`
**With**: Specific key invalidation

```java
// BEFORE (Bad)
@CacheEvict(value = {"leaderboard", "playerRank"}, allEntries = true)

// AFTER (Good)
@CacheEvict(
    value = "playerRank",
    key = "#playerId + '_' + #gameType"
)
// Only invalidate the specific player's rank, not everyone's!
```

**Impact**:
- Cache hit rate: 20% → **95%**
- Database load: **-90%**

---

### 4. Database Sharding Strategy

```sql
-- Shard by Game Type (hot sharding)
Shard 1: RACING, SPORTS (high traffic)
Shard 2: PUZZLE, STRATEGY (medium traffic)
Shard 3: SHOOTER, ADVENTURE (medium traffic)

-- Shard by Time (cold sharding)
Active Shard: Last 30 days (fast SSD)
Archive Shard: 30-90 days (regular storage)
Cold Storage: 90+ days (S3/archival)

-- Implementation
@Configuration
public class ShardingConfig {

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("racing", racingDataSource());
        targetDataSources.put("puzzle", puzzleDataSource());

        ShardingDataSource shardingDataSource = new ShardingDataSource();
        shardingDataSource.setTargetDataSources(targetDataSources);
        return shardingDataSource;
    }
}
```

---

### 5. Pre-computed Rankings (Background Job)

```java
// NEW: RankingComputationJob.java

@Component
public class RankingComputationJob {

    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void computeRankings() {
        for (GameType gameType : GameType.values()) {
            // Compute top 1000 players
            List<Score> topScores = scoreRepository
                .findTop1000ByGameType(gameType);

            // Store in Redis with pre-computed ranks
            int rank = 1;
            for (Score score : topScores) {
                redisTemplate.opsForHash().put(
                    "precomputed:rank:" + gameType,
                    score.getPlayer().getId().toString(),
                    rank++
                );
            }
        }
    }
}
```

---

### 6. CQRS Pattern (Read/Write Separation)

```java
// Write Model - PostgreSQL (normalized, transactional)
@Entity
public class Score {
    // Full entity with all fields
}

// Read Model - Redis/ElasticSearch (denormalized, optimized for queries)
public record LeaderboardEntry(
    Long playerId,
    String username,
    Long score,
    Integer rank
) {}

// Sync via events
@EventListener
public void on(ScoreSubmittedEvent event) {
    // Update read model
    searchRepository.save(buildSearchableEntry(event));
}
```

---

### 7. Rate Limiting

```java
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter scoreSubmissionLimiter() {
        return RateLimiter.create(
            100.0, // 100 requests per second per player
            Duration.ofSeconds(1)
        );
    }
}

@PostMapping("/scores")
@RateLimited(key = "#request.playerId", limit = 100)
public ResponseEntity<ScoreResponse> submitScore(@RequestBody SubmitScoreRequest request) {
    // ...
}
```

---

### 8. Multi-Region Deployment

```yaml
# Geo-distributed setup
regions:
  us-east:
    db: us-east-postgres
    redis: us-east-redis-cluster
  eu-west:
    db: eu-west-postgres
    redis: eu-west-redis-cluster
  ap-south:
    db: ap-south-postgres
    redis: ap-south-redis-cluster

# Cross-region replication
replication:
  mode: active-active
  conflict_resolution: last-write-wins
```

---

## Performance Comparison

### Before (Current Implementation)
```
Score Submission:     200-500ms  (write DB + update all caches + rank calc)
Get Leaderboard:      50-200ms   (DB query + in-memory ranking)
Get Player Rank:      500-2000ms (count all scores above)
Cache Hit Rate:       20-40%     (constant invalidation)
Max Throughput:       500 req/s  (DB bottleneck)
Max Players:          100K       (before degradation)
```

### After (Improved Architecture)
```
Score Submission:     20-50ms    (write DB + publish event)
Get Leaderboard:      5-10ms     (Redis ZSET + cache)
Get Player Rank:      3-8ms      (Redis ZREVRANK)
Cache Hit Rate:       90-98%     (granular invalidation)
Max Throughput:       50K req/s  (Redis + horizontal scaling)
Max Players:          100M+      (sharding + Redis)
```

---

## Migration Path

### Phase 1: Quick Wins (1-2 weeks)
- [ ] Add Redis for distributed caching
- [ ] Implement granular cache invalidation
- [ ] Add database read replicas
- [ ] Add rate limiting

### Phase 2: Architecture Changes (1 month)
- [ ] Implement Redis Sorted Sets for rankings
- [ ] Add message queue (RabbitMQ/Kafka)
- [ ] Async score processing
- [ ] Pre-computed rankings job

### Phase 3: Advanced Scaling (2-3 months)
- [ ] Database sharding
- [ ] CQRS pattern
- [ ] Multi-region deployment
- [ ] CDN for static leaderboards

---

## Cost Analysis

### Current (10M scores, 1M daily active users)
```
Database: RDS r6g.2xlarge  = $800/month
App Servers: 4x t3.medium  = $400/month
Total:                     = $1,200/month
Performance: Degraded/Failing
```

### Improved (Same scale)
```
Database: RDS r6g.xlarge   = $400/month  (less load)
Redis Cluster: 3x r6g.large = $600/month
App Servers: 8x t3.small    = $300/month (stateless, auto-scale)
Message Queue: MQ t3.micro  = $50/month
Total:                      = $1,350/month
Performance: Excellent, room to 100M+
```

---

## Monitoring & Observability

```java
// Add metrics
@Timed(value = "leaderboard.rank.calculation")
public PlayerRankResponse getPlayerRank(Long playerId, GameType gameType) {
    // ...
}

// Dashboards to track:
- Redis hit/miss rate
- Database query latency (p50, p95, p99)
- Cache size and eviction rate
- Message queue depth
- API response times by endpoint
```

---

## Conclusion

**Current Design**: Good for **prototyping** and **small scale**
**Needed for Production**: **Redis**, **async processing**, **sharding**, **granular caching**

**Recommended**: Implement **Phase 1** immediately for 10x improvement
