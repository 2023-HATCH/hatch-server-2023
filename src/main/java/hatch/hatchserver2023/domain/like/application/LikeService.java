package hatch.hatchserver2023.domain.like.application;


import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.like.repository.LikeRepository;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import hatch.hatchserver2023.global.config.redis.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final RedisCacheUtil redisCacheUtil;

    public LikeService(LikeRepository likeRepository, VideoRepository videoRepository, UserRepository userRepository, RedisCacheUtil redisCacheUtil){
        this.likeRepository = likeRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.redisCacheUtil = redisCacheUtil;
    }



    /**
     * 좋아요 추가
     *
     * @param videoId
     * @param user
     * @return likeUuid
     */
    public UUID addLike(UUID videoId, User user){
        // 비디오 존재 유무 확인
        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));

        //이미 좋아요를 눌렀다면
        if(isAlreadyLiked(video, user)){
            // 새로운 좋아요를 만들지 않고 에러 발생
            throw new VideoException(VideoStatusCode.ALREADY_LIKED);

        } else{
            // 좋아요를 누르지 않은 상태면, 새로운 좋아요 생성
            Like like = Like.builder()
                    .videoId(video)
                    .userId(user)
                    .build();

//            likeRepository.save(like);
            // redis 에 좋아요 데이터 저장, 좋아요 수 저장
            redisCacheUtil.addLike(video.getId(), user.getId());

            return like.getUuid();
        }
    }


    /**
     * 좋아요 삭제
     *
     * @param videoId
     * @param user
     */
    public void deleteLike(UUID videoId, User user){

        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));

//        Like like = likeRepository.findByVideoIdAndUserId(video, user)
//                .orElseThrow(() -> new VideoException(VideoStatusCode.LIKE_NOT_FOUND));

//        likeRepository.delete(like);
        redisCacheUtil.deleteLike(video, user);
    }


    /**
     * 어느 사용자의 좋아요 누른 영상 목록 조회
     *
     * @param userId
     * @param pageable
     * @return videoList
     */
    public Slice<Video> getLikedVideoList(UUID userId, Pageable pageable){

        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new AuthException(UserStatusCode.UUID_NOT_FOUND));

        Slice<Like> likeSlice = likeRepository.findAllByUserId(user, pageable);
        List<Like> likeList = likeSlice.getContent();

        //각 좋아요에서 영상 얻어오기
        List<Video> videoList = new ArrayList<>();

        for(Like like : likeList){
            videoList.add(like.getVideoId());
        }

        //paginaton 적용
        //no-offset
        Slice<Video> videoSlice = new SliceImpl<>(videoList, pageable, likeSlice.hasNext());

        return videoSlice;
    }


    //TODO: 최적화 방법 고민
    //TODO: 혹은 Video Entity에 likeCount 자체에 Formula로 쿼리 매핑해두기

    /**
     * 한 동영상의 좋아요 갯수 세기
     *
     * @param videoId
     * @return videoCount
     */

    public long countLike(UUID videoId){

        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));

        return likeRepository.countByVideoId(video);
    }


    /**
     * 이미 좋아요를 눌렀는지 확인
     *
     * @param video
     * @param user
     * @return isLiked
     */
    public boolean isAlreadyLiked(Video video, User user){
        log.info("isAlreadyLiked");
        return redisCacheUtil.isLiked(video, user);
    }


}
