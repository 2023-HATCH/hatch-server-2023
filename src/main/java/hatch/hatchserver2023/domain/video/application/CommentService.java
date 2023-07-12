package hatch.hatchserver2023.domain.video.application;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.video.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.repository.CommentRepository;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, VideoRepository videoRepository, UserRepository userRepository){
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }


    //댓글 등록
    public Comment createComment(String content, UUID videoId, UUID userId){
        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
        //TODO: 작성자 추가
//        User user = userRepository.findByUuid(userId)
//                .orElseThrow(() -> new AuthException(UserStatusCode.UUID_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(content)
//                .userId(user)
                .videoId(video)
                .build();

        commentRepository.save(comment);

        return comment;
    }



    //비디오의 댓글 목록 조회
    public List<Comment> getCommentList(UUID videoId) {

        Video video = videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));

        List<Comment> commentList = commentRepository.findAllByVideoId(video);

        return commentList;
    }
}
