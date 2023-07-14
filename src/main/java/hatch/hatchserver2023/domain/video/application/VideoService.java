package hatch.hatchserver2023.domain.video.application;


import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Like;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.domain.VideoHashtag;
import hatch.hatchserver2023.domain.video.repository.CommentRepository;
import hatch.hatchserver2023.domain.video.repository.LikeRepository;
import hatch.hatchserver2023.domain.video.repository.VideoHashtagRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.apache.commons.fileupload.FileItem;


import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Service s3Service;
    private final VideoHashtagRepository videoHashtagRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public VideoService(VideoRepository videoRepository, S3Service s3Service, VideoHashtagRepository videoHashtagRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.videoRepository = videoRepository;
        this.s3Service = s3Service;
        this.videoHashtagRepository = videoHashtagRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    @Value("${DEFAULT_THUMBNAIL_URL}")
    private String DEFAULT_THUMBNAIL_URL;


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

        //해시태그 매핑 테이블의 정보 삭제
        List<VideoHashtag> mapList = videoHashtagRepository.findAllByVideoId(video);
        for(VideoHashtag map : mapList) {
            videoHashtagRepository.delete(map);
        }

        //좋아요 데이터 삭제
        List<Like> likeList = likeRepository.findAllByVideoId(video);
        for(Like like : likeList){
            likeRepository.delete(like);
        }

        //댓글 데이터 삭제
        List<Comment> commentList = commentRepository.findAllByVideoId(video);
        for(Comment comment : commentList){
            commentRepository.delete(comment);
        }

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
     * @return Slice<Video>
     */
    public Slice<Video> findByRandom(Pageable pageable) {
        Slice<Video> slice = videoRepository.findAllOrderByRandom(pageable);

        return slice;
    }

    /**
     * 영상 목록 조회 - 최신순
     * - pagination 적용
     *
     * @param pageable
     * @return Slice<Video>
     */
    public Slice<Video> findByCreatedTime(Pageable pageable) {
        Slice<Video> slice = videoRepository.findAllByOrderByCreatedTimeDesc(pageable);
        return slice;
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
        String videoUrl = s3Service.uploadToVideo(video, user);

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
            thumbnailUrl = uploadImg(multipartFileThumbnail, user);

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
