package hatch.hatchserver2023.domain.comment.api;


import hatch.hatchserver2023.domain.comment.dto.CommentResponseDto;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.comment.application.CommentService;
import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.comment.dto.CommentRequestDto;
import hatch.hatchserver2023.domain.video.dto.VideoResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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



    /**
     * 댓글 등록
     *
     * @param user
     * @param videoId
     * @param request
     * @return commentUuid
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")    //로그인한 사용자만 사용 가능
    @PostMapping("/{videoId}")
    public ResponseEntity<CommonResponse> registerComment(@AuthenticationPrincipal User user,
                                             @PathVariable UUID videoId,
                                             @RequestBody @Valid CommentRequestDto request){

        Comment comment = commentService.createComment(request.getContent(), videoId, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.COMMENT_REGISTER_SUCCESS,
                CommentResponseDto.CreateComment.toDto(comment)
        ));
    }


    /**
     * 댓글 삭제
     * @param user
     * @param commentId
     * @return isSuccess
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse> deleteComment(@AuthenticationPrincipal User user,
                                           @PathVariable UUID commentId){

        commentService.deleteComment(commentId, user);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.COMMENT_DELETE_SUCCESS,
                VideoResponseDto.IsSuccess.toDto(true)
        ));
    }


    /**
     * 영상의 댓글 목록 조회
     *
     * @param videoId
     * @return commentList
     */
//    @PreAuthorize("hasAnyRole('ROLE_ANONYMOUS')")
    @GetMapping("/{videoId}")
    public ResponseEntity<CommonResponse> getCommentList(@PathVariable UUID videoId){

        List<Comment> commentList = commentService.getCommentList(videoId);

        return ResponseEntity.ok(CommonResponse.toResponse(
                VideoStatusCode.GET_COMMENT_LIST_SUCCESS,
                CommentResponseDto.GetCommentList.toDto(commentList)
        ));
    }
}
