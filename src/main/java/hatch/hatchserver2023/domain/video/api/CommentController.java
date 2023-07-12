package hatch.hatchserver2023.domain.video.api;


import hatch.hatchserver2023.domain.video.application.CommentService;
import hatch.hatchserver2023.domain.video.domain.Comment;
import hatch.hatchserver2023.domain.video.dto.CommentRequestDto;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService){
        this.commentService = commentService;
    }



    // 댓글 등록
    @PostMapping("/{videoId}")
    public ResponseEntity<?> registerComment(@PathVariable UUID videoId,
                                             @RequestBody CommentRequestDto request){

        // TODO: User 추가
        Comment comment = commentService.createComment(request.getContent(), videoId, UUID.randomUUID());

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.COMMENT_REGISTER_SUCCESS,
                VideoResponseDto.CreateComment.toDto(comment)
        ));

    }

    //댓글 목록 조회
    @GetMapping("/{videoId}")
    public ResponseEntity<?> getCommentList(@PathVariable UUID videoId){

        List<Comment> commentList = commentService.getCommentList(videoId);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_COMMENT_LIST_SUCCESS,
                VideoResponseDto.GetCommentList.toDto(commentList)
        ));

    }
}
