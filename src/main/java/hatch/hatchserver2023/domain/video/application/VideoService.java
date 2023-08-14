package hatch.hatchserver2023.domain.video.application;


import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.apache.commons.fileupload.FileItem;


import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Service s3Service;
    private final LikeService likeService;
    private final VideoCacheUtil videoCacheUtil;

    public VideoService(VideoRepository videoRepository, S3Service s3Service, LikeService likeService, VideoCacheUtil videoCacheUtil) {
        this.videoRepository = videoRepository;
        this.s3Service = s3Service;
        this.likeService = likeService;
        this.videoCacheUtil = videoCacheUtil;
    }

    @Value("${DEFAULT_THUMBNAIL_URL}")
    private String DEFAULT_THUMBNAIL_URL;

    @Value("${CLOUDFRONT_URL}")
    private String CLOUDFRONT_URL;


    /**
     * 영상 하나 상세 조회
     *
     * @param uuid
     * @return single video
     */
    public Video findOne(UUID uuid){
        return getVideo(uuid);
    }


    /**
     * 영상 삭제
     *
     * @param uuid
     * @return isSuccess
     */
    public void deleteOne(UUID uuid){
        Video video = getVideo(uuid);

        // S3에 올라가 있는 동영상과 썸네일 또한 삭제
        s3Service.delete(video.getVideoUrl());
        s3Service.delete(video.getThumbnailUrl());

        //Video 데이터 DB에서 삭제
        videoRepository.delete(video);
    }

    // Video 하나 가져오는 메서드
    private Video getVideo(UUID uuid) {
        return videoRepository.findByUuid(uuid)
                .orElseThrow(() -> (new VideoException(VideoStatusCode.VIDEO_NOT_FOUND)));
    }


    /**
     * 영상 목록 조회 - 랜덤
     * - pagination 적용
     *
     * @param pageable
     * @return Slice<VideoInfo>
     */
    public Slice<VideoModel.VideoInfo> findByRandom(User loginUser, Pageable pageable) {
        Slice<Video> videoSlice = videoRepository.findAllOrderByRandom(pageable);

        List<VideoModel.VideoInfo> videoInfoList;

        // 비회원: isLiked가 모두 false
        if(loginUser == null) {
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(false)
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }
        //회원: isLiked 여부 확인
        else {
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(likeService.isAlreadyLiked(one, loginUser))
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }
        Slice<VideoModel.VideoInfo> videoInfoSlice = new SliceImpl<>(videoInfoList, pageable, videoSlice.hasNext());

        return videoInfoSlice;
    }

    /**
     * 영상 목록 조회 - 최신순
     * - pagination 적용
     *
     * @param pageable
     * @return Slice<VideoInfo>
     */
    public Slice<VideoModel.VideoInfo> findByCreatedAt(User loginUser, Pageable pageable) {
        Slice<Video> videoSlice = videoRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<VideoModel.VideoInfo> videoInfoList;

        //비회원: isLiked가 모두 false
        if(loginUser == null) {
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(false)
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }
        //회원: 좋아요 누른 여부 isLiked 확인
        else{
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(likeService.isAlreadyLiked(one, loginUser))
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }
        Slice<VideoModel.VideoInfo> videoInfoSlice = new SliceImpl<>(videoInfoList, pageable, videoSlice.hasNext());

        return videoInfoSlice;
    }

    /**
     * 영상 목록 조회 - 좋아요 순
     * - pagination 적용
     *
     * @param pageable
     * @return Slice<Video>
     */
    public Slice<Video> findByLikeCount(Pageable pageable) {
        Slice<Video> slice = videoRepository.findAllByOrderByLikeCountDesc(pageable);
        return slice;
    }

    /**
     * 영상 목록 조회 - 조회수 순
     * - pagination 적용
     *
     * @param pageable
     * @return Slice<Video>
     */
    public Slice<Video> findByViewCount(Pageable pageable) {
        Slice<Video> slice = videoRepository.findAllByOrderByViewCountDesc(pageable);
        return slice;
    }



    /**
     * 썸네일 사진 업로드
     *
     * @param image
     * @return thumbnail_url
     */
    public String uploadImg(MultipartFile image, User user) {
//        log.info("[VideoService] Single Img Upload");
        return s3Service.uploadToVideo(image, user);
    }

    /**
     * 동영상 생성 & 업로드
     * - 썸네일 추출 포함
     *
     * @param user
     * @param video
     * @param title
     * @param tag
     * @return video
     */
    public Video createVideo(MultipartFile video, User user, String title, String tag) {
        log.info("[VideoService] Single video Upload");

        // 영상 업로드하고
        String s3VideoUrl = s3Service.uploadToVideo(video, user);
        String videoUrl = CLOUDFRONT_URL + s3VideoUrl.substring(49);

        // 영상을 File로 바꾸고
        File diskVideo = multipartfileToFile(video);

        // 영상 길이 얻고
        int length = getVideoLength(diskVideo);

        // 썸네일 추출하고
        String thumbnailUrl = extractThumbnail(diskVideo, user, length);

        // 임시 디스크 동영상 삭제
        removeTempFile(diskVideo);

        // 영상 builder로 만들고
        Video uploadedVideo = Video.builder()
                .userId(user)
                .title(title)
                .tag(tag)
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .length(length)
                .build();

        // 영상 저장
        videoRepository.save(uploadedVideo);

        // Video 객체 return
        return uploadedVideo;
    }

    /**
     * MultipartFile -> File 전환
     *
     * @param multipartFile
     * @return file
     */
    private File multipartfileToFile(MultipartFile multipartFile) {
        File file = new File(System.getProperty("user.dir") +"\\"+ multipartFile.getOriginalFilename());
        try {
            multipartFile.transferTo(file);

        } catch (IOException e) {
            log.error("[VideoService][extractThumbnail] Fail to Convert MultipartFile to File");
            removeTempFile(file);
            e.printStackTrace();
            throw new VideoException(VideoStatusCode.CONVERT_MULTIPARTFILE_TO_FILE_FAIL);
        }
        return file;
    }


    /**
     * 썸네일 추출 & 업로드
     *
      * @param source, length, user
     * @return thumbnailUrl
     */
    private String extractThumbnail(File source, User user, int length) {
        log.info("[VideoService] Start Extract Thumbnail");

        final String EXTENSION = "png";
        String thumbnailUrl = "";

        // 썸네일 파일 생성
        File thumbnail = new File(source.getParent() + "\\" + source.getName().split("\\.")[0] + "." + EXTENSION);

        try {
            SeekableByteChannel channel = NIOUtils.readableChannel(source);
            FrameGrab frameGrab = FrameGrab.createFrameGrab(channel);

            // 영상 중간 프레임의 데이터
            // length는 milliseconds 단위이므로 1000으로 나눔
            frameGrab.seekToSecondPrecise(length/1000/2);

            Picture picture = frameGrab.getNativeFrame();

            // 썸네일 파일에 복사
            ImageIO.write(AWTUtil.toBufferedImage(picture), EXTENSION, thumbnail);
            ImageIO.setUseCache(false);


            // File to MultipartFile 전환
            MultipartFile multipartFileThumbnail = fileToMultipartFile(thumbnail);

            // 썸네일 S3에 업로드
            String s3ThumbnailUrl = uploadImg(multipartFileThumbnail, user);

            //cloudfront URL로 변경
            thumbnailUrl = CLOUDFRONT_URL + s3ThumbnailUrl.substring(49);

            //임시 파일 지우기(다른 메서드)
            removeTempFile(thumbnail);
            channel.close();


        } catch (Exception e) {
            log.warn("[VideoService] Fail to Extract Thumbnail. Instead, Apply Default Image");
            // 실패했을 경우에 기본 이미지 사용
            // 에러는 내지 않을 예정
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        }

        return thumbnailUrl;
    }


    /**
     * File을 MultipartFile로 전환
     *
     * @param file
     * @return multipartFile
     * @throws IOException
     */
    private MultipartFile fileToMultipartFile(File file) throws IOException {
        FileItem fileItem = new DiskFileItem("tempFileName", Files.probeContentType(file.toPath()), false, file.getName(), (int) file.length(), file.getParentFile());

        try {
            InputStream input = new FileInputStream(file);
            OutputStream os = fileItem.getOutputStream();
            IOUtils.copy(input, os);
            input.close();
            os.flush();
            os.close();

        } catch (IOException e) {
            log.error("[FAIL] fail to convert file to multipartFile");
            e.printStackTrace();
            throw new VideoException(VideoStatusCode.CONVERT_FILE_TO_MULTIPARTFILE_FAIL);
        }

        return new CommonsMultipartFile(fileItem);
    }

    /**
     * 디스크 임시 파일 삭제
     * - 실패해도 에러 발생시키지 않음
     *
     * @param targetFile
     */
    private void removeTempFile(File targetFile) {
        if (targetFile.exists()){
            if (targetFile.delete()) {
                return;
            }
            // 파일은 존재하지만 삭제에는 실패
            else {
                try {
                    // 2초 쉬고 다시 삭제 시도
                    Thread.sleep(2000);

                    if (targetFile.delete()) {
                        return;
                    }
                } catch (InterruptedException e) {
                    // TODO: 이게 무슨 에러지?
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                log.warn("[FAIL] Fail to delete temp file");
            }
        }
        else{
            log.warn("[FAIL] Temp file does not exist");
        }
    }

    /**
     * 영상 길이 추출
     * - 추출에 실패하면 -1 반환
     * - 실패해도 에러 발생시키지 않음
     *
     * @param video
     * @return videoLength
     */
    private int getVideoLength(File video) {

        try {
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));
            double durationInSeconds = frameGrab.getVideoTrack().getMeta().getTotalDuration();
            int durationInMilliseconds = (int) (durationInSeconds * 1000);

            log.info("Video length: {} seconds", durationInSeconds);
            log.info("Video length: {} milliseconds", durationInMilliseconds);

            return durationInMilliseconds;
        } catch (Exception e) {
            // 에러 발생은 시키지 않음
            log.warn("Video Length extract failed", e);
        }

        return -1;
    }

}
