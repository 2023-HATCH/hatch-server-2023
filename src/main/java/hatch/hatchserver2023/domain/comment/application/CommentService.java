package hatch.hatchserver2023.domain.comment.application;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.comment.repository.CommentRepository;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
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

    public CommentService(CommentRepository commentRepository, VideoRepository videoRepository){
        this.commentRepository = commentRepository;
        this.videoRepository = videoRepository;
    }


    /**
     * 댓글 등록
     *
     * @param content
     * @param videoId
     * @param user
     * @return comment
     */
    public Comment createComment(String content, UUID videoId, User user){

        Video video = getVideo(videoId);

        Comment comment = Comment.builder()
                .content(content)
                .userId(user)
                .videoId(video)
                .build();

        commentRepository.save(comment);

        return comment;
    }


    /**
     * 댓글 삭제
     *
     * @param commentId
     * @param user
     */
    public void deleteComment(UUID commentId, User user){

        Comment comment = commentRepository.findByUuid(commentId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.COMMENT_NOT_FOUND));

        //자신이 작성한 댓글이 아닌 다른 댓글을 삭제하려고 하면 에러 발생
        if(!comment.getUserId().getUuid().equals(user.getUuid())){
            throw new VideoException(VideoStatusCode.NOT_YOUR_COMMENT);
        }

        commentRepository.delete(comment);
    }


    /**
     * 비디오의 댓글 목록 조회
     *
     * @param videoId
     * @return commentList
     */
    public List<Comment> getCommentList(UUID videoId) {

        Video video = getVideo(videoId);

        List<Comment> commentList = commentRepository.findAllByVideoId(video);

        return commentList;
    }


    // videoUuid로 Video 객체를 DB에서 찾아오는 함수
    private Video getVideo(UUID videoId) {
        return videoRepository.findByUuid(videoId)
                .orElseThrow(() -> new VideoException(VideoStatusCode.VIDEO_NOT_FOUND));
    }
}
