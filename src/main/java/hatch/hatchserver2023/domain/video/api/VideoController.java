package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService){
        this.videoService = videoService;
    }


    //TODO: 원래는 uuid를 넣어줘야하지만 지금은 테스트용으로 id 사용
    @GetMapping("/{id}")
    public Video getOneVideo(@PathVariable Long id) {
        Video video = videoService.findOne(id);

        return video;
    }


    @PostMapping("/upload1")
    public ResponseEntity<?> uploadVideo1(@RequestParam MultipartFile video,
                                         @RequestParam String title,
                                         @RequestParam String tag) {
        log.info("[VideoController][upload video] Request multiPartFile's ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, title, tag);

        // TODO: 해시태그 파싱해서 따로 저장하는 것 추가
        // TODO: 작성자를 Video에 추가하는 것

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS, VideoResponseDto.CreateVideo.toDto(createdVideo)));
    }

    @PostMapping("/upload2")
    public ResponseEntity<?> uploadVideo2(MultipartFile video,
                                         String title,
                                         String tag) {
        log.info("[VideoController][upload video] Request multiPartFiles ContentType >> " + video.getContentType());

        Video createdVideo = videoService.createVideo(video, title, tag);

        // TODO: 해시태그 파싱해서 따로 저장하는 것 추가
        // TODO: 작성자를 Video에 추가하는 것

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.VIDEO_UPLOAD_SUCCESS, VideoResponseDto.CreateVideo.toDto(createdVideo)));
    }
}
