package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.video.domain.Video;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VideoResponseDto {

    @Builder
    @Getter
    public static class VideoUuid {

        private UUID uuid;

        public static VideoUuid toDto(Video video) {
            return VideoUuid.builder()
                    .uuid(video.getUuid())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetVideo {
        private UUID uuid;
        private String title;
        private String tag;

        private UserResponseDto.CommunityUserInfo user;
        private boolean isLiked;
        private String videoUrl;
        private String thumbnailUrl;
        private Integer likeCount;
        private Integer commentCount;
        private Integer length;
        private ZonedDateTime createdAt;

        //isLike가 없는 버전
        public static GetVideo toDto(Video video){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(video.getUserId());

            return GetVideo.builder()
                    .uuid(video.getUuid())
                    .title(video.getTitle())
                    .tag(video.getTag())
                    .user(userInfo)
                    .videoUrl(video.getVideoUrl())
                    .thumbnailUrl(video.getThumbnailUrl())
                    .likeCount(video.getLikeCount())
                    .commentCount(video.getCommentCount())
                    .length(video.getLength())
                    .createdAt(video.getCreatedAt())
                    .build();
        }

        //isLike가 있는 버전
        public static GetVideo toDto(Video video, boolean isLiked){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(video.getUserId());

            return GetVideo.builder()
                    .uuid(video.getUuid())
                    .title(video.getTitle())
                    .tag(video.getTag())
                    .user(userInfo)
                    .isLiked(isLiked)
                    .videoUrl(video.getVideoUrl())
                    .thumbnailUrl(video.getThumbnailUrl())
                    .likeCount(video.getLikeCount())
                    .commentCount(video.getCommentCount())
                    .length(video.getLength())
                    .createdAt(video.getCreatedAt())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetVideoList {

        private List<GetVideo> videoList;

        private Boolean isLast;

        // 비회원 조회
        public static GetVideoList toDto(Slice<Video> slice){

            List<GetVideo> getVideos = new ArrayList<>();

            for (Video video : slice.getContent()) {
                //dto로 만들어 add
                getVideos.add(GetVideo.toDto(video));
            }
            return GetVideoList.builder()
                    .videoList(getVideos)
                    .isLast(slice.isLast())
                    .build();
        }

        // 회원 조회: 좋아요 리스트를 함께 받는 경우
        public static GetVideoList toDto(Slice<Video> slice, List<Boolean> likeList){

            List<GetVideo> getVideos = new ArrayList<>();
            List<Video> videoList = slice.getContent();

            for(int idx = 0; idx < slice.getSize(); idx++){

                //dto로 만들어 add
                getVideos.add(GetVideo.toDto(videoList.get(idx), likeList.get(idx)));
            }

            return GetVideoList.builder()
                    .videoList(getVideos)
                    .isLast(slice.isLast())
                    .build();
        }

        //인자로 List<Video>를 받고 isLast가 null인 버전
        public static GetVideoList toDto(List<Video> videoList){

            List<GetVideo> getVideos = new ArrayList<>();

            for(Video video : videoList) {
                getVideos.add(GetVideo.toDto(video));
            }
            return GetVideoList.builder()
                    .videoList(getVideos)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class GetHashtagList {

        private List<String> tagList;

        public static GetHashtagList toDto(List<String> tagList) {
            return GetHashtagList.builder()
                    .tagList(tagList)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class IsSuccess {

        private boolean isSuccess;

        public static IsSuccess toDto(boolean isSuccess) {
            return VideoResponseDto.IsSuccess.builder()
                    .isSuccess(isSuccess)
                    .build();
        }

    }

}
