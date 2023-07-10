package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.user.domain.User;
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
    }
}
