package hatch.hatchserver2023.global.config.redis;

import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
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

//    /**
//     * List 에 데이터를 저장하는 메서드
//     * @param key
//     * @param data
//     */
//    public void pushValuesList(String key, String data) {
//        ListOperations<String, String> values = redisTemplate.opsForList();
//        values.rightPush(key, data);
//    }

    /**
     * Set 에 데이터를 저장하는 메서드
     * @param key
     * @param data
     */
    public void setValuesSet(String key, String data) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        values.add(key, data);
    }

    /**
     * Sorted Set 에 데이터를 저장하는 메서드
     * @param key
     * @param data
     * @param score : 정렬 기준
     */
    public void setValuesZSet(String key, String data, int score) {
        ZSetOperations<String, String> values = redisTemplate.opsForZSet();
        values.add(key, data, score);
    }


//    /**
//     * List 에 데이터를 저장하는 메서드
//     * @param key
//     * @param start
//     * @param end
//     */
//    public List<String> getValuesList(String key, Long start, Long end) {
//        ListOperations<String, String> values = redisTemplate.opsForList();
//        return values.range(key, start, end);
//    }

//    /**
//     * Hash 에 데이터를 저장하는 메서드
//     * @param key : 해시 키값. 해시의 이름같은 것
//     * @param hashKey : 이 해시 내에서의 찾으려는 키값. RDB의 필드명과 비슷함
//     * @param data : 데이터
//     */
//    public void setValuesHash(String key, Object hashKey, Object data) {
//        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
//        values.put(key, hashKey, data);
//    }


    ///////// == get method == /////////
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
     * Set 에서 특정 범위의 데이터들을 가져오는 메서드
     * @param key
     */
    public Set<String> getValuesSet(String key) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        return values.members(key);
    }

    /**
     * Set 의 원소 개수를 반환하는 메서드
     * @param key
     * @return
     */
    public Long getSetSize(String key) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        return values.size(key);
    }

    /**
     * Set 에 특정 데이터가 존재하는지 확인하는 메서드
     * @param key
     * @return
     */
    public boolean isSetDataExist(String key, String data) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        Boolean isExist = values.isMember(key, data);
        return Boolean.TRUE.equals(isExist); //null 일 경우 false로 반환하도록 함
    }

    /**
     * Sorted Set 에서 특정 범위의 데이터들을 가져오는 메서드
     * @param key
     * @param start
     * @param end
     */
    public Set<String> getValuesZSet(String key, int start, int end) {
        ZSetOperations<String, String> values = redisTemplate.opsForZSet();
        return values.range(key, start, end);
    }

    /**
     * Sorted Set 에서 해당 키의 모든 데이터들을 가져오는 메서드
     * @param key
     */
    public Set<String> getValuesZSetAll(String key) {
        ZSetOperations<String, String> values = redisTemplate.opsForZSet();
        return values.range(key, 0, -1);
    }

    /**
     * Set 에서 해당 데이터를 삭제하는 메서드
     * @param key
     * @param data
     */
    public void removeValuesSet(String key, String data) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        values.remove(key, data);
    }

//    /**
//     * Hash 에서 데이터를 가져오는 메서드
//     * @param key : 해시 키값. 객체의 이름과 비슷함
//     * @param hashKey : 이 해시 내에서의 찾으려는 키값. 객체의 필드명과 비슷함
//     */
//    public Object getValuesHash(String key, Object hashKey) {
//        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
//        return values.get(key, hashKey);
//    }

    /**
     * 해당 Hash 의 전체 데이터를 가져오는 매서드
     * @param key : 해시 키값. 객체의 이름과 비슷함
     */
//    public List<Object> getValuesHashAll(String key) {
//        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
//        return values.values();
//    }

    /**
     * 키값에 해당하는 데이터를 삭제하는 메서드
     * @param key
     */
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }




    /**
     * 개발용으로만 쓰는 redis db 전체 삭제 메서드
     */
    public void deleteAll() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}
