package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.video.domain.Video;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class VideoModel {

    @Builder
    @Getter
    @ToString
    public static class VideoInfo {

        private Video video;
        private boolean isLiked;
        private int likeCount;
        private int commentCount;
        private int viewCount;

    }
}
