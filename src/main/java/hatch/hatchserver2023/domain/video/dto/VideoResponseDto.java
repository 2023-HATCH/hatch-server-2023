package hatch.hatchserver2023.domain.video.dto;

import hatch.hatchserver2023.domain.video.domain.Video;
import lombok.Builder;
import lombok.Getter;

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
}
