package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.user.domain.User;
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
    public static class CreateVideo {

        private UUID uuid;

        public static VideoResponseDto.CreateVideo toDto(Video video) {
            return CreateVideo.builder()
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

        // TODO: 반환용 UserDTO 만들기(userId랑 nickname, 프로필 사진)
        private User userId;
        private String url;
        private Integer likeCount;
        private Integer length;
        private ZonedDateTime createdTime;

        public static GetVideo toDto(Video video){
            return GetVideo.builder()
                    .uuid(video.getUuid())
                    .title(video.getTitle())
                    .tag(video.getTag())
                    .userId(video.getUserId())
                    .url(video.getVideoUrl())
                    .likeCount(video.getLikeCount())
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
    public static class CreateComment {

        private UUID uuid;
        private String content;
        // TODO: 반환용 UserDto 적용
//        private User userId;


        public static CreateComment toDto(Comment comment){
            return CreateComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
//                    .userId(comment.getUserId())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetComment {

        private UUID uuid;
        private String content;
        // TODO: 반환용 UserDto 적용
//        private User userId;


        public static GetComment toDto(Comment comment){
            return GetComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
//                    .userId(comment.getUserId())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetCommentList {

        private List<GetComment> commentList;

        // TODO: 반환용 UserDto 적용
//        private User userId;

        public static GetCommentList toDto(List<Comment> comments){

            List<GetComment> getComments = new ArrayList<>();

            for (Comment comment : comments) {
                //dto로 만들어 add
                getComments.add(GetComment.toDto(comment));
            }

            return GetCommentList.builder()
                    .commentList(getComments)
//                    .userId(comment.getUserId())
                    .build();
        }
    }
}
