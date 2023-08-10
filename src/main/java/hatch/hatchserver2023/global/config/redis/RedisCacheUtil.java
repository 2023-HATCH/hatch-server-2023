package hatch.hatchserver2023.global.config.redis;

import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.like.repository.LikeRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisCacheUtil {
    private final String KEY_CACHE_LIKE_INFO = "like:updated:"; //Hash. hashKey는 userId, value는 add또는 delete
    private final String KEY_CACHE_LIKE_COUNT = "like:count:"; //String
    private final String KEY_CACHE_VIDEO_VIEW_COUNT = "video:viewCount:"; // String

    private final String CACHE_LIKE_INFO_ADD = "add";
    private final String CACHE_LIKE_INFO_DELETE = "delete";

    private final RedisDao redisDao;
    private final VideoRepository videoRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public RedisCacheUtil(RedisDao redisDao, VideoRepository videoRepository, LikeRepository likeRepository, UserRepository userRepository) {
        this.redisDao = redisDao;
        this.videoRepository = videoRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    // redis에 데이터 존재 여부 확인
//    private boolean isLikeExist(long videoId, long userId) {
//        return redisDao.isHashKeyExist(toLikeInfoKey(videoId), String.valueOf(userId));
//    }

    /**
     * 해당하는 키값으로 redis 데이터 존재 확인 -> redis 데이터 반환 또는 RDB에서 가져옴
     * @param video
     * @param user
     * @return
     */
    public boolean isLiked(Video video, User user) {
        // 레디스에 존재하는지 확인
        Object statusObject = redisDao.getValuesHash(toLikeInfoKey(video.getId()), user.getId());

        // 레디스에 없으면 RDB 조회
        if(statusObject == null) {
//            throw new VideoException(VideoStatusCode.LIKE_NOT_FOUND_REDIS);
            return likeRepository.findByVideoIdAndUserId(video, user).isPresent();
        }

        // 레디스에 add 로 저장되어있으면 좋아요 누른 거 맞음
        return statusObject.toString().equals(CACHE_LIKE_INFO_ADD);
    }

    public void addLike(long videoId, long userId) {
        saveLike(videoId, userId, CACHE_LIKE_INFO_ADD);

        updateLikeCount(videoId, 1);
    }

    public void deleteLike(long videoId, long userId) {
        saveLike(videoId, userId, CACHE_LIKE_INFO_DELETE);

        updateLikeCount(videoId, -1);
    }




    //////////// move to RDB ///////////

    /**
     * 주기적으로 redis의 좋아요 관련 데이터를 RDB에 저장하고 redis 데이터 삭제
     */
    // @Scheduled 로 주기적으로 DB에 업데이트 // TODO : Refactor
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
    private void moveLikeDataToRDB() {
        // 좋아요 데이터 Likeinfo Hash
        Cursor<String> likeKeyCursor = redisDao.getKeys(KEY_CACHE_LIKE_INFO+"*");

        List<Like> addLikes = new ArrayList<>();
        List<Like> deleteLikes = new ArrayList<>();
        while (likeKeyCursor.hasNext()) {
            String key = likeKeyCursor.next();
            Video video;
            try{
                video = getVideoFromKey(key);
            } catch (VideoException e){
                log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                continue;
            }

            // hashKey인 userId 돌면서 user 데이터 가져와 like 객체 생성
            Set<String> userIds = redisDao.getHashKeys(key);
            for(String userId : userIds) {
                String status = String.valueOf(redisDao.getValuesHash(String.valueOf(video.getId()), userId));
                Like like;
                try {
                    like = getLikeByUserIdVideo(Long.parseLong(userId), video);
                } catch (VideoException e){
                    log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                    continue;
                }

                if(status.equals(CACHE_LIKE_INFO_ADD)){
                    addLikes.add(like);
                }
                else if(status.equals(CACHE_LIKE_INFO_DELETE)){
                    deleteLikes.add(like);
                }
            }
        }

        likeRepository.saveAll(addLikes);
        likeRepository.deleteAll(deleteLikes);
        redisDao.deleteValues(likeKeyCursor.stream().collect(Collectors.toSet()));

        // 좋아요 수 LikeCount String
        Cursor<String> countKeyCursor = redisDao.getKeys(KEY_CACHE_LIKE_COUNT+"*");

        List<Video> videos = new ArrayList<>();
        while (countKeyCursor.hasNext()) {
            String key = countKeyCursor.next();
            String viewCount = redisDao.getValues(key);
            Video video = getVideoFromKey(key);

            video.updateLikeCount(Integer.parseInt(viewCount));
            videos.add(video);
        }

        videoRepository.saveAll(videos);
        redisDao.deleteValues(countKeyCursor.stream().collect(Collectors.toSet()));
    }

    private Video getVideoFromKey(String key) throws VideoException {
        // 키값에서 videoId 추출
        long videoId = getIdFromKey(key);

        // video 데이터
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
    }

    private Like getLikeByUserIdVideo(long userId, Video video) throws VideoException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new VideoException(UserStatusCode.USER_NOT_FOUND));

        return Like.builder()
                .userId(user)
                .videoId(video).build();
    }


    //////////// private util method ///////////
    private void saveLike(long videoId, long userId, String status) {
        redisDao.setValuesHash(toLikeInfoKey(videoId), userId, status);
    }

    private String toLikeInfoKey(long videoId) {
        return KEY_CACHE_LIKE_INFO +videoId;
    }

    private boolean isLikeCountExist(long videoId) {
        return redisDao.isKeyExist(toLikeCountKey(videoId));
    }

    private String toLikeCountKey(long videoId) {
        return KEY_CACHE_LIKE_COUNT+videoId;
    }

    private void updateLikeCount(long videoId, int diff) {
        String key = toLikeInfoKey(videoId);
        Integer count;
        if(isLikeCountExist(videoId)){
            count = Integer.parseInt(redisDao.getValues(key))+diff;
        }
        else {
            Video video = videoRepository.findById(videoId).orElseThrow(() ->
                    new VideoException(VideoStatusCode.VIDEO_NOT_FOUND)
            );
            count = video.getLikeCount();
        }

        redisDao.setValues(key, String.valueOf(count)); // TODO : String.valueOf 이거 redisDao에 setValues 오버로딩해서 다 숨길까?
    }

    private long getIdFromKey(String key) {
        String[] keySplit = key.split(":");
        return Long.parseLong(keySplit[keySplit.length-1]);
    }
}
