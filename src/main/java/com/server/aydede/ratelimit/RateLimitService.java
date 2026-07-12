package com.server.aydede.ratelimit;

import java.time.Duration;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.github.bucket4j.ConsumptionProbe;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RateLimitService {

    private final RateLimitProperties properties;
    private final ProxyManager<String> proxyManager;

    public RateLimitService(RateLimitProperties properties, LettuceConnectionFactory connectionFactory) {
        this.properties = properties;

        RedisClient redisClient = (RedisClient) connectionFactory.getNativeClient();

        StatefulRedisConnection<String, byte[]> connection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofHours(1)))
                .build();
    }

    public RateLimitResult tryConsume(String firebaseUid) {
        String key = "rl:session:user:" + firebaseUid;

        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.userPerMinute())
                        .refillGreedy(properties.userPerMinute(), Duration.ofMinutes(1))
                        .build())
                .addLimit(Bandwidth.builder()
                        .capacity(properties.userPerHour())
                        .refillGreedy(properties.userPerHour(), Duration.ofHours(1))
                        .build())
                .build();
        return consume(key, config);
    }

    public RateLimitResult tryConsumeIp(String ip) {
        String key = "rl:session:ip:" + ip;
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(properties.ipPerMinute())
                        .refillGreedy(properties.ipPerMinute(), Duration.ofMinutes(1))
                        .build())
                .build();
        return consume(key, config);
    }

    private RateLimitResult consume(String key, BucketConfiguration config) {
        try {
            var bucket = proxyManager.builder().build(key, config);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                return RateLimitResult.ok();
            }
            long waitNanos = probe.getNanosToWaitForRefill();
            long waitSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(waitNanos));
            return RateLimitResult.denied(waitSeconds);
        } catch (Exception e) {
            log.warn("Rate limit Redis unavailable, fail-open: {}", e.getMessage());
            return RateLimitResult.ok();
        }
    }

    public record RateLimitResult(boolean allowed, long retryAfterSeconds) {
        public static RateLimitResult ok() {
            return new RateLimitResult(true, 0);
        }

        public static RateLimitResult denied(long retryAfterSeconds) {
            return new RateLimitResult(false, retryAfterSeconds);
        }
    }
}
