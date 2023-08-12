package hatch.hatchserver2023.domain.like;

import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.like.repository.LikeRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
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
import java.util.Set;

@Slf4j
@Component
public class LikeCacheUtil {
    private final String KEY_CACHE_LIKE_INFO = "like:updated:"; //Hash. hashKey는 userId, value는 add또는 delete
    private final String KEY_CACHE_LIKE_COUNT = "like:count:"; //String
//    private final String KEY_CACHE_VIDEO_VIEW_COUNT = "video:viewCount:"; // String

    private final String CACHE_LIKE_INFO_ADD = "add";
    private final String CACHE_LIKE_INFO_DELETE = "delete";

    private final RedisDao redisDao;
    private final VideoRepository videoRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public LikeCacheUtil(RedisDao redisDao, VideoRepository videoRepository, LikeRepository likeRepository, UserRepository userRepository) {
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
     * @return 좋아요 눌렀으면 true, 삭제했거나 누른적 없으면 false
     */
    public boolean isLiked(Video video, User user) {
        // 레디스에 존재하는지 확인
        Object statusObject = redisDao.getValuesHash(toLikeInfoKey(video.getId()), String.valueOf(user.getId()));

        // 레디스에 없으면 RDB 조회
        if(statusObject == null) {
            return likeRepository.findByVideoIdAndUserId(video, user).isPresent();
        }
        else{
            // 레디스에 있으면 add 로 저장되어있는지
            return statusObject.toString().equals(CACHE_LIKE_INFO_ADD);
        }
    }

    /**
     * 좋아요 추가
     * @param video
     * @param user
     */
    public void addLike(Video video, User user) {
        log.info("[REDIS] addLike");

        //이미 좋아요를 눌렀다면
        if(isLiked(video, user)){
            // 새로운 좋아요를 만들지 않고 에러 발생
            throw new VideoException(VideoStatusCode.ALREADY_LIKED);
        }

        saveLike(video.getId(), user.getId(), CACHE_LIKE_INFO_ADD);
        updateLikeCount(video.getId(), 1);
    }

    /**
     * 좋아요 삭제
     * @param video
     * @param user
     */
    public void deleteLike(Video video, User user) {
        log.info("[REDIS] deleteLike");

        if(isLiked(video, user)) {
            saveLike(video.getId(), user.getId(), CACHE_LIKE_INFO_DELETE);
            updateLikeCount(video.getId(), -1);
        }
        else{
            log.info("deleteLike : never liked or already deleted like");
        }
    }




    //////////// move to RDB ///////////

    /**
     * 주기적으로 redis의 좋아요 관련 데이터를 RDB에 저장하고 redis 데이터 삭제
     */
    // @Scheduled 로 주기적으로 DB에 업데이트 // TODO : Refactor
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
//    @Scheduled(fixedRate = 1000 * 10) // 10초 마다 실행(테스트용)
    private void moveLikeDataToRDB() {
        log.info("[SCHEDULED] moveLikeDataToRDB : start at {}", ZonedDateTime.now());

        // 좋아요 데이터 Likeinfo Hash
        Cursor<String> likeKeyCursor = redisDao.getKeys(KEY_CACHE_LIKE_INFO+"*"); // 좋아요 데이터 key값 목록

        // add, delete 된 like 데이터들 가져오기
        List<Like> addLikes = new ArrayList<>();
        List<Like> deleteLikes = new ArrayList<>();
        List<String> infoKeys = new ArrayList<>();
        while (likeKeyCursor.hasNext()) {
            String key = likeKeyCursor.next();
            infoKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            // video
            Video video;
            try{
                video = getVideoFromKey(key);
            } catch (VideoException e){
                log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                continue;
            }

            // user, like : hashKey인 userId 돌면서 user 데이터 가져와 like 객체 생성
            Set<String> userIds = redisDao.getHashKeys(key);
            for(String userId : userIds) {
                log.info("[SCHEDULED] hashKey userId : {}", userId);
                User user;
                try {
                    user = getUser(Long.parseLong(userId));
                } catch (VideoException e){
                    log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                    continue;
                }
                String status = String.valueOf(redisDao.getValuesHash(toLikeInfoKey(video.getId()), String.valueOf(userId)));
                log.info("[SCHEDULED] status : {}", status);

                // add인가 delete인가에 따라서 like 생성
                Like like;
                if(status.equals(CACHE_LIKE_INFO_ADD)){
                    //add 이면 like 객체 새로 생성
                    like = Like.builder()
                            .userId(user)
                            .videoId(video).build();
                    addLikes.add(like);
                    log.info("[SCHEDULED] added like info user : {}, video : {}", like.getUserId().getNickname(), like.getVideoId().getTitle());
                }
                else if(status.equals(CACHE_LIKE_INFO_DELETE)){
                    // delete 이면 RDB 에서 삭제할 데이터를 가져옴
                    try {
                        like = getLike(video, user);
                    } catch (VideoException e){
                        log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                        continue;
                    }
                    deleteLikes.add(like);
                    log.info("[SCHEDULED] deleted like info id : {}", like.getId());
                }
            }
        }
        log.info("[SCHEDULED] get like info END");
        log.info("[SCHEDULED] added like info list size : {}", addLikes.size());
        log.info("[SCHEDULED] deleted like info list size : {}", deleteLikes.size());

        //로그 삭제
        String nick = (addLikes.size()==0) ? null : addLikes.get(0).getUserId().getNickname();
        log.info("[SCHEDULED] added like info fist item user : {}", nick);
//        String nick2 = (deleteLikes.size()==0) ? null : deleteLikes.get(0).getUserId().getNickname();
        Long id = (deleteLikes.size()==0) ? null : deleteLikes.get(0).getId();
        log.info("[SCHEDULED] deleted like info fist item id : {}", id);

        likeRepository.saveAll(addLikes);
        likeRepository.deleteAll(deleteLikes);
        redisDao.deleteValues(infoKeys);
        log.info("[SCHEDULED] redis like info saved and deleted");

        // 좋아요 수 LikeCount String
        Cursor<String> countKeyCursor = redisDao.getKeys(KEY_CACHE_LIKE_COUNT+"*"); // 좋아요 수 키값 목록

        List<Video> videos = new ArrayList<>();
        List<String> countKeys = new ArrayList<>();
        while (countKeyCursor.hasNext()) {
            String key = countKeyCursor.next();
            countKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            String viewCount = redisDao.getValues(key);
            Video video = getVideoFromKey(key);

            video.updateLikeCount(Integer.parseInt(viewCount));
            videos.add(video);
            log.info("[SCHEDULED] like count likeCount : {}, video : {}", video.getLikeCount(), video.getTitle());
        }
        log.info("[SCHEDULED] get like count END");
        log.info("[SCHEDULED] like count list size : {}", videos.size());

        //로그 삭제
        Integer count = (videos.size()==0) ? null : videos.get(0).getLikeCount();
        log.info("[SCHEDULED] like count fist item likeCount : {}", count);

        videoRepository.saveAll(videos);
        redisDao.deleteValues(countKeys);
        log.info("[SCHEDULED] redis like count saved and deleted");

        log.info("[SCHEDULED] moveLikeDataToRDB : finish at {}", ZonedDateTime.now());
    }

    private Video getVideoFromKey(String key) throws VideoException {
        // 키값에서 videoId 추출
        long videoId = getIdFromKey(key);

        // video 데이터
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
    }

    private User getUser(long userId) throws VideoException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new VideoException(UserStatusCode.USER_NOT_FOUND));
    }

    private Like getLike(Video video, User user) throws VideoException {
        return likeRepository.findByVideoIdAndUserId(video, user)
                .orElseThrow(() -> new VideoException(VideoStatusCode.LIKE_NOT_FOUND));
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
        log.info("[REDIS] updateLikeCount");
        String key = toLikeCountKey(videoId);
        Integer count;
        if(isLikeCountExist(videoId)){
            count = Integer.parseInt(redisDao.getValues(key))+diff;
        }
        else {
            Video video = videoRepository.findById(videoId).orElseThrow(() ->
                    new VideoException(VideoStatusCode.VIDEO_NOT_FOUND)
            );
            count = video.getLikeCount()+diff;
        }

        redisDao.setValues(key, String.valueOf(count)); // TODO : String.valueOf 이거 redisDao에 setValues 오버로딩해서 다 숨길까?
    }

    private long getIdFromKey(String key) {
        String[] keySplit = key.split(":");
        return Long.parseLong(keySplit[keySplit.length-1]);
    }
}
