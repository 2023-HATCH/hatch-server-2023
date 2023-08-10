//package hatch.hatchserver2023.global.config.redis;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//@Slf4j
//public class RedisTest { //레디스 사용 연습해보는 클래스
//
//    private final RedisDao redisDao;
//
//    public RedisTest(RedisDao redisDao) {
//        this.redisDao = redisDao;
//    }
//
//    @Test
//    public void getNotExistKeyTest() {
//        String value = redisDao.getValues("not:exist:key");
////        String value = redisDao.getValues("");
////        String value = 1+1;
//        log.info("getNotExistKeyTest value : {}", value);
//    }
//}
