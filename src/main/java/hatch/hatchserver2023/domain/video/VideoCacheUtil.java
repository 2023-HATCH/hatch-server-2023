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
        Object countObject = redisDao.getValues(key);

        int view;
        if(countObject == null) {
            // redis 에 없으면 RDB에서 가져온 데이터 사용
            view = video.getViewCount();
        } else {
            // redis 에 있으면 그걸로 사용
            view = Integer.parseInt(countObject.toString());
        }

        log.info("getViewCount view : {}", view);
        return view;
    }


    // TODO : 코드 중복 - like count 좋아요수 로직이랑 99% 똑같음
    /**
     * 주기적으로 redis의 조회수 데이터를 RDB에 저장하고 redis 에서 삭제
     */
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
//    @Scheduled(fixedRate = 1000 * 10) // 10초 마다 실행(테스트용)
    private void moveViewCountDataToRDB() {
        log.info("[SCHEDULED] moveLikeDataToRDB : start at {}", ZonedDateTime.now());

        // 조회수 데이터 String
        Cursor<String> viewKeyCursor = redisDao.getKeys(KEY_CACHE_VIDEO_VIEW_COUNT+"*"); // 조회수 데이터 key값 목록

        List<Video> videos = new ArrayList<>();
        List<String> viewKeys = new ArrayList<>();
        makeViewCountedVideos(viewKeyCursor, videos, viewKeys);

        log.info("[SCHEDULED] get viewCount END");
        log.info("[SCHEDULED] viewCount list size : {}", videos.size());

        //로그
//        Integer count = (videos.size()==0) ? null : videos.get(0).getViewCount();
//        log.info("[SCHEDULED] viewCount fist item viewCount : {}", view);

        videoRepository.saveAll(videos);
        redisDao.deleteValues(viewKeys);
        log.info("[SCHEDULED] moveLikeDataToRDB : finish at {}", ZonedDateTime.now());
    }

    private void makeViewCountedVideos(Cursor<String> viewKeyCursor, List<Video> videos, List<String> viewKeys) {
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
    }


    private String toViewCountKey(long videoId) {
        return KEY_CACHE_VIDEO_VIEW_COUNT+videoId;
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
