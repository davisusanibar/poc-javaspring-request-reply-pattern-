package com.ejemplo.kafka.redis;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageProcessor {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisMessageProcessor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addPendingRequest(String correlationId, CompletableFuture<String> future) {
        redisTemplate.opsForValue().set("pending:" + correlationId, future, 30, TimeUnit.SECONDS);
    }

    public void resolvePendingRequest(String correlationId, String result) {
        CompletableFuture<String> future = (CompletableFuture<String>) redisTemplate.opsForValue().get("pending:" + correlationId);
        if (future != null) {
            future.complete(result);
            redisTemplate.delete("pending:" + correlationId);
        }
    }

    public void rejectPendingRequest(String correlationId, Throwable error) {
        CompletableFuture<String> future = (CompletableFuture<String>) redisTemplate.opsForValue().get("pending:" + correlationId);
        if (future != null) {
            future.completeExceptionally(error);
            redisTemplate.delete("pending:" + correlationId);
        }
    }

    public void storeProcessedMessage(String correlationId, String result) {
        redisTemplate.opsForValue().set("message:" + correlationId, new MessageData(result, System.currentTimeMillis()), 60, TimeUnit.SECONDS);
    }

    public String getProcessedMessage(String correlationId) {
        MessageData data = (MessageData) redisTemplate.opsForValue().get("message:" + correlationId);
        return data != null ? data.result : null;
    }

    public void cleanupExpiredMessages(long expirationTime) {
        // Redis automatically expires keys, so we don't need to implement this method
    }

    public void cleanupExpiredRequests(long timeoutDuration) {
        // Redis automatically expires keys, so we don't need to implement this method
    }

    private static class MessageData {
        final String result;
        final long timestamp;

        MessageData(String result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
    }
}
