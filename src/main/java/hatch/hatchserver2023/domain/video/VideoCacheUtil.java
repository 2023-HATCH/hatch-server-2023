package hatch.hatchserver2023.domain.video;

import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class VideoCacheUtil {
    private final String KEY_CACHE_VIDEO_VIEW_COUNT = "video:viewCount:"; // String
    private final String KEY_CACHE_VIDEO_COMMENT_COUNT = "video:commentCount:"; // String

    private final RedisDao redisDao;
    private final VideoRepository videoRepository;

    public VideoCacheUtil(RedisDao redisDao, VideoRepository videoRepository) {
        this.redisDao = redisDao;
        this.videoRepository = videoRepository;
    }

    /**
     * 조회수 증가 (redis)
     * @param video
     */
    public void addViewCount(Video video) {
        log.info("[REDIS] addViewCount");

        int view = getViewCount(video);

        redisDao.setValues(toViewCountKey(video.getId()), view+1);
        log.info("addViewCount view+1 : {}", view+1);
    }

    /**
     * 조회수 조회 (redis) : 수빈님 연결 부탁하기
     * @param video
     * @return
     */
    public int getViewCount(Video video) { // TODO : 모든 영상 조회 부분에 적용
        log.info("[REDIS] getViewCount");

        // redis 조회
        String key = toViewCountKey(video.getId());
        int view = getStringCacheData(key, video);

        log.info("getViewCount view : {}", view);
        return view;
    }

    /**
     * 댓글수 증가 (redis)
     * @param video
     */
    public void increaseCommentCount(Video video) {
        log.info("[REDIS] increaseCommentCount");

        int increasedCount = saveCommentCount(video, +1);
        log.info("increaseCommentCount increasedCount : {}", increasedCount);
    }

    /**
     * 댓글수 감소 (redis)
     * @param video
     */
    public void decreseCommentCount(Video video) {
        log.info("[REDIS] addCommentCount");

        int decreaseCount = saveCommentCount(video, -1);
        log.info("addCommentCount decreaseCount : {}", decreaseCount);
    }

    /**
     * 댓글수 조회 (redis) : 수빈님 연결 부탁하기
     * @param video
     * @return
     */
    public int getCommentCount(Video video) { // TODO : 모든 영상 조회 부분에 적용
        log.info("[REDIS] getCommentCount");

        // redis 조회
        String key = toCommentCountKey(video.getId());
        int commentCount = getStringCacheData(key, video);

        log.info("getCommentCount commentCount : {}", commentCount);
        return commentCount;
    }


    // TODO : 코드 중복 - like count 좋아요수 로직이랑 99% 똑같음
    /**
     * 주기적으로 redis의 조회수 데이터를 RDB에 저장하고 redis 에서 삭제
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
//    @Scheduled(fixedRate = 1000 * 10) // 10초 마다 실행(테스트용)
    private void moveCountDataOfVideoToRDB() {
        log.info("[SCHEDULED] moveCountDataOfVideoToRDB : start at {}", ZonedDateTime.now());

        // 조회수 데이터 String
        Cursor<String> viewKeyCursor = redisDao.getKeys(KEY_CACHE_VIDEO_VIEW_COUNT+"*"); // 조회수 데이터 key값 목록
        Cursor<String> commentCountKeyCursor = redisDao.getKeys(KEY_CACHE_VIDEO_COMMENT_COUNT+"*"); // 댓글수 데이터 key값 목록

        List<Video> videos = new ArrayList<>();
        List<String> viewKeys = new ArrayList<>();
        List<String> commentCountKeys = new ArrayList<>();
        makeCountedVideos(viewKeyCursor, commentCountKeyCursor, videos, viewKeys, commentCountKeys);

        log.info("[SCHEDULED] get viewCount END");
        log.info("[SCHEDULED] viewCount list size : {}", videos.size());

        //로그
//        Integer count = (videos.size()==0) ? null : videos.get(0).getViewCount();
//        log.info("[SCHEDULED] viewCount fist item viewCount : {}", view);

        videoRepository.saveAll(videos);
        redisDao.deleteValues(viewKeys);
        log.info("[SCHEDULED] moveCountDataOfVideoToRDB : finish at {}", ZonedDateTime.now());
    }

    private void makeCountedVideos(Cursor<String> viewKeyCursor, Cursor<String> commentCountKeyCursor, List<Video> videos, List<String> viewKeys, List<String> commentCountKeys) {
        while (viewKeyCursor.hasNext()) {
            // 이번 키값
            String key = viewKeyCursor.next();
            viewKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            // 조회수 데이터 redis에서, video 데이터 RDB에서 가져옴
            String view = redisDao.getValues(key);
            Video video = getVideo(key);

            // video 데이터에 조회수 반영하여 모음
            video.updateViewCount(Integer.parseInt(view));
            videos.add(video);
            log.info("[SCHEDULED] viewCount : {}, video : {}", video.getViewCount(), video.getTitle());
        }

        // TODO : 댓글수 데이터 동기화 로직 추가
    }


    private String toViewCountKey(long videoId) {
        return KEY_CACHE_VIDEO_VIEW_COUNT+videoId;
    }

    private String toCommentCountKey(long videoId) {
        return KEY_CACHE_VIDEO_COMMENT_COUNT+videoId;
    }

    /**
     * 댓글수 증감하여 Redis에 저장하는 메서드
     * @param video
     * @param diff
     * @return
     */
    private int saveCommentCount(Video video, int diff) {
        int commentCount = getCommentCount(video);

        redisDao.setValues(toCommentCountKey(video.getId()), commentCount+diff);
        return commentCount;
    }

    /**
     * Redis 에서 String자료형의 데이터 존재여부 확인 후 Redis또는 RDB에서 가져옴
     * @param key
     * @param video
     * @return
     */
    private int getStringCacheData(String key, Video video) {
        Object countObject = redisDao.getValues(key);

        int commentCount;
        if(countObject == null) {
            // redis 에 없으면 RDB에서 가져온 데이터 사용
            commentCount = video.getViewCount();
        } else {
            // redis 에 있으면 그걸로 사용
            commentCount = Integer.parseInt(countObject.toString());
        }
        return commentCount;
    }

    // TODO : LikeCacheUtil 과 중복
    private Video getVideo(String key) throws VideoException {
        // 키값에서 videoId 추출
        long videoId = getIdFromKey(key);

        // video 데이터
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
    }

    // TODO : LikeCacheUtil 과 중복
    private long getIdFromKey(String key) {
        String[] keySplit = key.split(":");
        return Long.parseLong(keySplit[keySplit.length-1]);
    }
}
