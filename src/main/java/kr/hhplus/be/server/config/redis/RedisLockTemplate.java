package kr.hhplus.be.server.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisLockTemplate {

    private final RedissonClient redissonClient;

    public RedisLockTemplate(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T executeWithLock(String key, int waitTime, int leaseTime, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = false;

        try {
            // waitTime 동안 락을 시도하고 leaseTime 동안 락을 유지
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("Redis Lock 획득 실패");
                throw new RuntimeException("락을 획득할 수 없습니다: " + key);
            }
            return callback.doWithLock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("Redis Lock 처리 중 인터럽트");
            throw new RuntimeException("락 처리 중 인터럽트 발생: " + key, e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    public interface LockCallback<T> {
        T doWithLock();
    }
}
