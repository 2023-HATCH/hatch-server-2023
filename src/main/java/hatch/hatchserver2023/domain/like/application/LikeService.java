package hatch.hatchserver2023.domain.like.application;


import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.like.repository.LikeRepository;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import hatch.hatchserver2023.domain.like.LikeCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;
    private final VideoCacheUtil videoCacheUtil;
    private final LikeCacheUtil likeCacheUtil;

    public LikeService(LikeRepository likeRepository, VideoRepository videoRepository, UserRepository userRepository, VideoCacheUtil videoCacheUtil, LikeCacheUtil likeCacheUtil){
        this.likeRepository = likeRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
        this.videoCacheUtil = videoCacheUtil;
        this.likeCacheUtil = likeCacheUtil;
    }



    /**
     * 좋아요 추가
     *
     * @param videoId
     * @param user
     * @return likeUuid
     */
    public void addLike(UUID videoId, User user){
        // 비디오 존재 유무 확인
        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));

        // redis 에 좋아요 데이터 저장, 좋아요 수 저장
        likeCacheUtil.addLike(video, user);
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

        likeCacheUtil.deleteLike(video, user);
    }


    /**
     * 어느 사용자의 좋아요 누른 영상 목록 조회
     *
     * @param userId
     * @param loginUser
     * @param pageable
     * @return likedVideoList
     */
    public Slice<VideoModel.VideoInfo> getLikedVideoList(UUID userId, User loginUser, Pageable pageable){

        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new AuthException(UserStatusCode.UUID_NOT_FOUND));

        Slice<Like> likeSlice = likeRepository.findAllByUserId(user, pageable);

        //각 좋아요에서 영상 얻어오기
        List<VideoModel.VideoInfo> videoInfoList;

        //비회원: liked는 모두 false
        if (loginUser == null) {
            videoInfoList = likeSlice.stream()
                    .map(like -> VideoModel.VideoInfo.builder()
                            .video(like.getVideo())
                            .isLiked(false)
                            .viewCount(videoCacheUtil.getViewCount(like.getVideo()))
                            .likeCount(videoCacheUtil.getLikeCount(like.getVideo()))
                            .commentCount(videoCacheUtil.getCommentCount(like.getVideo()))
                            .build())
                    .collect(Collectors.toList());
        }
        //회원: 영상 좋아요 여부 liked 지정
        else{
            videoInfoList = likeSlice.stream()
                    .map(like -> VideoModel.VideoInfo.builder()
                            .video(like.getVideo())
                            .isLiked(isAlreadyLiked(like.getVideo(), loginUser))
                            .viewCount(videoCacheUtil.getViewCount(like.getVideo()))
                            .likeCount(videoCacheUtil.getLikeCount(like.getVideo()))
                            .commentCount(videoCacheUtil.getCommentCount(like.getVideo()))
                            .build())
                    .collect(Collectors.toList());
        }

        //paginaton 적용
        //no-offset
        Slice<VideoModel.VideoInfo> videoInfoSlice = new SliceImpl<>(videoInfoList, pageable, likeSlice.hasNext());

        return videoInfoSlice;
    }


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
        return likeCacheUtil.isLiked(video, user);
    }


}
