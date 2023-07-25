package hatch.hatchserver2023.global.config.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

@Component
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisDao(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * String 데이터를 저장하는 메서드
     * @param key
     * @param data
     */
    public void setValues(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    /**
     * String 데이터를 일정 시간 동안만 저장하는 메서드
     * @param key
     * @param data
     * @param duration
     */
    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    /**
     * Sorted Set 에 데이터를 저장하는 메서드
     * @param key
     * @param data
     * @param score : 정렬 기준
     */
    public void setValuesZSet(String key, String data, Double score) {
        ZSetOperations<String, String> values = redisTemplate.opsForZSet();
        values.add(key, data, score);
    }

    /**
     * Sorted Set 에서 특정 범위의 데이터들을 가져오는 메서드
     * @param key
     * @param data
     * @param start
     * @param end
     */
    public Set<String> getValuesZSet(String key, String data, Long start, Long end) {
        ZSetOperations<String, String> values = redisTemplate.opsForZSet();
        return values.range(key, start, end);
    }

    /**
     * 키값에 해당하는 String 데이터를 가져오는 메서드
     * @param key
     * @return
     */
    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }

    /**
     * 키값에 해당하는 데이터를 삭제하는 메서드
     * @param key
     */
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

}
