package hatch.hatchserver2023.domain.like;

import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.like.repository.LikeRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
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

    private final String CACHE_LIKE_INFO_ADD = "add";
    private final String CACHE_LIKE_INFO_DELETE = "delete";

    private final RedisDao redisDao;
    private final VideoCacheUtil videoCacheUtil;
    private final VideoRepository videoRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public LikeCacheUtil(RedisDao redisDao, VideoCacheUtil videoCacheUtil, VideoRepository videoRepository, LikeRepository likeRepository, UserRepository userRepository) {
        this.redisDao = redisDao;
        this.videoCacheUtil = videoCacheUtil;
        this.videoRepository = videoRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
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
        videoCacheUtil.updateLikeCount(video, 1);
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
            videoCacheUtil.updateLikeCount(video, -1);
        }
        else{
            log.info("deleteLike : never liked or already deleted like");
        }
    }

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


    //////////// move to RDB ///////////

    /**
     * 주기적으로 redis의 좋아요 관련 데이터를 RDB에 저장하고 redis 데이터 삭제
     */
    // @Scheduled 로 주기적으로 DB에 업데이트
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
//    @Scheduled(fixedRate = 1000 * 10) // 10초 마다 실행(테스트용)
    private void moveLikeDataToRDB() {
        log.info("[SCHEDULED] moveLikeDataToRDB : start at {}", ZonedDateTime.now());

        // 좋아요 데이터 Likeinfo Hash
        Cursor<String> likeKeyCursor = redisDao.getKeys(KEY_CACHE_LIKE_INFO+"*"); // 좋아요 데이터 key값 목록
        moveLikeInfo(likeKeyCursor);

        log.info("[SCHEDULED] moveLikeDataToRDB : finish at {}", ZonedDateTime.now());
    }

    // TODO : Refactor
    /**
     * 좋아요 정보 데이터를 RDB로 옮기는 메서드
     * @param likeKeyCursor
     */
    private void moveLikeInfo(Cursor<String> likeKeyCursor) {
        // add, delete 된 like 데이터들 가져오기
        List<Like> addLikes = new ArrayList<>();
        List<Like> deleteLikes = new ArrayList<>();
        List<String> infoKeys = new ArrayList<>();
        while (likeKeyCursor.hasNext()) {
            String key = likeKeyCursor.next();
            infoKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            // video 가져옴
            Video video;
            try{
                video = videoCacheUtil.getVideoFromRDB(key);
            } catch (VideoException e){
                log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                continue;
            }

            // user, like : hashKey인 userId 돌면서 user 데이터 가져와 like 객체 생성
            makeLikes(key, video, addLikes, deleteLikes);
        }
        log.info("[SCHEDULED] get like info END");
        log.info("[SCHEDULED] added like info list size : {}", addLikes.size());
        log.info("[SCHEDULED] deleted like info list size : {}", deleteLikes.size());

        //로그
//        String nick = (addLikes.size()==0) ? null : addLikes.get(0).getUserId().getNickname();
//        log.info("[SCHEDULED] added like info fist item user : {}", nick);
//        Long id = (deleteLikes.size()==0) ? null : deleteLikes.get(0).getId();
//        log.info("[SCHEDULED] deleted like info fist item id : {}", id);

        likeRepository.saveAll(addLikes);
        likeRepository.deleteAll(deleteLikes);
        redisDao.deleteValues(infoKeys);
        log.info("[SCHEDULED] redis like info saved and deleted");
    }

    // TODO : Refactor
    /**
     * RDB에 저장 또는 RDB에서 삭제할 좋아요 목록을 만드는 메서드
     * @param key
     * @param video
     * @param addLikes
     * @param deleteLikes
     */
    private void makeLikes(String key, Video video, List<Like> addLikes, List<Like> deleteLikes) {
        Set<String> userIds = redisDao.getHashKeys(key);
        for(String userId : userIds) {
            log.info("[SCHEDULED] hashKey userId : {}", userId);

            // user 가져옴
            User user;
            try {
                user = getUser(Long.parseLong(userId));
            } catch (VideoException e){
                log.info("moveLikeDataToRDB {}", e.getCode().getMessage());
                continue;
            }

            // user 와 video 로 status 값 확인
            String status = String.valueOf(redisDao.getValuesHash(toLikeInfoKey(video.getId()), String.valueOf(userId)));
            log.info("[SCHEDULED] status : {}", status);

            // status 가 add인가 delete인가에 따라서 like 생성
            Like like;
            if(status.equals(CACHE_LIKE_INFO_ADD)){
                //add 이면 like 객체 새로 생성
                like = Like.builder()
                        .user(user)
                        .video(video).build();
                addLikes.add(like);
                log.info("[SCHEDULED] added like info user : {}, video : {}", like.getUser().getNickname(), like.getVideo().getTitle());
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
//
//    private Video getVideo(String key) throws VideoException {
//        // 키값에서 videoId 추출
//        long videoId = getIdFromKey(key);
//
//        // video 데이터
//        return videoRepository.findById(videoId)
//                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
//    }

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


    private long getIdFromKey(String key) {
        String[] keySplit = key.split(":");
        return Long.parseLong(keySplit[keySplit.length-1]);
    }
}
