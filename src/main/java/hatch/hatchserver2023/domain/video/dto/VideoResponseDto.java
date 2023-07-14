package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import hatch.hatchserver2023.domain.video.domain.Comment;
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
        private ZonedDateTime createdTime;

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
                    .createdTime(video.getCreatedTime())
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
                    .createdTime(video.getCreatedTime())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetVideoList {

        private List<GetVideo> videoList;

        private Boolean isLast;

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


    @Builder
    @Getter
    public static class CreateComment {

        private UUID uuid;
        private String content;
        private UserResponseDto.CommunityUserInfo user;


        public static CreateComment toDto(Comment comment){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(comment.getUserId());

            return CreateComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
                    .user(userInfo)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetComment {

        private UUID uuid;
        private String content;
        private UserResponseDto.CommunityUserInfo user;

        private ZonedDateTime createdTime;


        public static GetComment toDto(Comment comment){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(comment.getUserId());

            return GetComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
                    .user(userInfo)
                    .createdTime(comment.getCreatedTime())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetCommentList {

        private List<GetComment> commentList;


        public static GetCommentList toDto(List<Comment> comments){

            List<GetComment> getComments = new ArrayList<>();

            for (Comment comment : comments) {
                //dto로 만들어 add
                getComments.add(GetComment.toDto(comment));
            }

            return GetCommentList.builder()
                    .commentList(getComments)
                    .build();
        }
    }

}
