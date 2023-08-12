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
import java.util.stream.Collectors;

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

        public static List<GetVideo> toDtos(List<VideoModel.VideoInfo> videoInfoList){
            return videoInfoList.stream()
                    .map(videoInfo -> GetVideo.toDto(videoInfo.getVideo(), videoInfo.isLiked()))
                    .collect(Collectors.toList());
        }
    }

    @Builder
    @Getter
    public static class GetVideoList {

        private List<GetVideo> videoList;

        private Boolean isLast;


        //인자로 List<GetVideo>를 받고 isLast도 직접 받음
        public static GetVideoList toDto(List<GetVideo> videoList, Boolean isLast){

            return GetVideoList.builder()
                    .videoList(videoList)
                    .isLast(isLast)
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
