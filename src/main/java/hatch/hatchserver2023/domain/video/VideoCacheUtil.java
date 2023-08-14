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

    private final String KEY_CACHE_VIDEO_COUNT_HASH = "video:count:"; // Hash. 한 Video 객체에 저장되는 count데이터들을 모아놓는 Hash

    private final String HASH_KEY_CACHE_VIDEO_LIKE_COUNT = "like"; // Hash 필드명
    private final String HASH_KEY_CACHE_VIDEO_VIEW_COUNT = "view";
    private final String HASH_KEY_CACHE_VIDEO_COMMENT_COUNT = "comment";

    private final RedisDao redisDao;
    private final VideoRepository videoRepository;

    public VideoCacheUtil(RedisDao redisDao, VideoRepository videoRepository) {
        this.redisDao = redisDao;
        this.videoRepository = videoRepository;
    }

    /**
     * 좋아요수 저장 메서드
     * @param video
     * @param diff
     */
    public void updateLikeCount(Video video, int diff) {
        log.info("[REDIS] updateLikeCount");

        int likeCount = getLikeCount(video);

        int updatedLikeCount = likeCount + diff;
        saveVideoCountData(video.getId(), HASH_KEY_CACHE_VIDEO_LIKE_COUNT, updatedLikeCount);
        log.info("updatedLikeCount : {}", updatedLikeCount);
    }

    /**
     * 좋아요수 조회 메서드 (redis적용)
     * @param video
     * @return
     */
    public int getLikeCount(Video video) {
        log.info("[REDIS] getLikeCount");
        // 레디스에 존재하는지 확인
        Object likeCountObject = redisDao.getValuesHash(toVideoCountDataKey(video.getId()), HASH_KEY_CACHE_VIDEO_LIKE_COUNT);

        // 레디스에 없으면 RDB 에서 가져온 데이터 반환
        if(likeCountObject == null) {
            return video.getLikeCount();
        }
        else{
            // 레디스에 있으면 그거 반환
            return Integer.parseInt(likeCountObject.toString());
        }
    }

    /**
     * 조회수 증가 (redis)
     * @param video
     */
    public void addViewCount(Video video) {
        log.info("[REDIS] addViewCount");

        int view = getViewCount(video);

        int updatedView = view + 1;
        saveVideoCountData(video.getId(), HASH_KEY_CACHE_VIDEO_VIEW_COUNT, updatedView);
        log.info("updatedView : {}", updatedView);
    }

    /**
     * 조회수 조회 (redis)
     * @param video
     * @return
     */
    public int getViewCount(Video video) {
        log.info("[REDIS] getViewCount");
        // 레디스에 존재하는지 확인
        Object commentCountObject = getVideoCountData(video.getId(), HASH_KEY_CACHE_VIDEO_VIEW_COUNT);

        // 레디스에 없으면 RDB 에서 가져온 데이터 반환
        if(commentCountObject == null) {
            return video.getViewCount();
        }
        else{
            // 레디스에 있으면 그거 반환
            return Integer.parseInt(commentCountObject.toString());
        }
    }

    /**
     * 댓글수 증가 (redis)
     * @param video
     */
    public void increaseCommentCount(Video video) {
        log.info("[REDIS] increaseCommentCount");

        int increasedCount = saveCommentCount(video, +1);
        log.info("increasedCount : {}", increasedCount);
    }

    /**
     * 댓글수 감소 (redis)
     * @param video
     */
    public void decreaseCommentCount(Video video) {
        log.info("[REDIS] decreaseCommentCount");

        int decreaseCount = saveCommentCount(video, -1);
        log.info("decreaseCount : {}", decreaseCount);
    }

    /**
     * 댓글수 조회 (redis)
     * @param video
     * @return
     */
    public int getCommentCount(Video video) {
        log.info("[REDIS] getCommentCount");

        // 레디스에 존재하는지 확인
        Object commentCountObject = getVideoCountData(video.getId(), HASH_KEY_CACHE_VIDEO_COMMENT_COUNT);

        // 레디스에 없으면 RDB 에서 가져온 데이터 반환
        if(commentCountObject == null) {
            return video.getCommentCount();
        }
        else{
            // 레디스에 있으면 그거 반환
            return Integer.parseInt(commentCountObject.toString());
        }
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
        Cursor<String> videoCountKeyCursor = redisDao.getKeys(KEY_CACHE_VIDEO_COUNT_HASH+"*"); // 비디오 카운트 데이터 key값 목록

        List<Video> videos = new ArrayList<>();
        List<String> videoCountKeys = new ArrayList<>();
        makeCountedVideos(videoCountKeyCursor, videos, videoCountKeys);

        log.info("[SCHEDULED] get videos END");
        log.info("[SCHEDULED] videos list size : {}", videos.size());

        //로그
//        Integer count = (videos.size()==0) ? null : videos.get(0).getViewCount();
//        log.info("[SCHEDULED] viewCount fist item viewCount : {}", view);

        videoRepository.saveAll(videos);
        redisDao.deleteValues(videoCountKeys);
        log.info("[SCHEDULED] moveCountDataOfVideoToRDB : finish at {}", ZonedDateTime.now());
    }

    private void makeCountedVideos(Cursor<String> videoCountKeyCursor, List<Video> videos, List<String> videoCountKeys) {
        while (videoCountKeyCursor.hasNext()) {
            // 이번 키값
            String key = videoCountKeyCursor.next();
            videoCountKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            // 키값에 해당하는 video 객체
            Video video = getVideoFromRDB(key);

            // 각 필드에 대해 값 가져와서 video 에 반영
            // 좋아요수
            Object likeCountObject = redisDao.getValuesHash(key, HASH_KEY_CACHE_VIDEO_LIKE_COUNT);
            if (likeCountObject != null) {
                video.updateLikeCount(Integer.parseInt(likeCountObject.toString()));
            }

            // 조회수
            Object viewCountObject = redisDao.getValuesHash(key, HASH_KEY_CACHE_VIDEO_VIEW_COUNT);
            if (viewCountObject != null) {
                video.updateViewCount(Integer.parseInt(viewCountObject.toString()));
            }

            // 댓글수
            Object commentCountObject = redisDao.getValuesHash(key, HASH_KEY_CACHE_VIDEO_COMMENT_COUNT);
            if (commentCountObject != null) {
                video.updateCommentCount(Integer.parseInt(commentCountObject.toString()));
            }

            // 데이터 반영 완료된 video 모음
            videos.add(video);
        }
    }



    /**
     * 댓글수 증감하여 Redis에 저장하는 메서드
     * @param video
     * @param diff
     * @return
     */
    private int saveCommentCount(Video video, int diff) {
        int commentCount = getCommentCount(video);

        int updatedCount = commentCount + diff;
        saveVideoCountData(video.getId(), HASH_KEY_CACHE_VIDEO_COMMENT_COUNT, updatedCount);

        return updatedCount;
    }

    /**
     * Redis 에서 String자료형의 데이터 존재여부 확인 후 Redis또는 RDB에서 가져옴 -> String 을 Hash로 바꿔서 쓸모 사라짐
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

    private void saveVideoCountData(long videoId, String hashKey, int count) {
        redisDao.setValuesHash(toVideoCountDataKey(videoId), hashKey, count);
    }

    private Object getVideoCountData(long videoId, String hashKey) {
        return redisDao.getValuesHash(toVideoCountDataKey(videoId), hashKey);
    }

    private String toVideoCountDataKey(long videoId) {
        return KEY_CACHE_VIDEO_COUNT_HASH +videoId;
    }


    public Video getVideoFromRDB(String key) throws VideoException {
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
