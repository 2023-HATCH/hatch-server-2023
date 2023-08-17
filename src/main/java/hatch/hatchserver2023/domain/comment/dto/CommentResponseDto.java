package hatch.hatchserver2023.domain.comment.dto;

import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentResponseDto {

    @Builder
    @Getter
    public static class CreateComment {

        private UUID uuid;
        private String content;
        private UserResponseDto.CommunityUserInfo user;


        public static CommentResponseDto.CreateComment toDto(Comment comment){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(comment.getUser());

            return CommentResponseDto.CreateComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
                    .user(userInfo)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetComment {

        private UUID uuid;
        private String content;
        private UserResponseDto.CommunityUserInfo user;

        private ZonedDateTime createdAt;


        public static CommentResponseDto.GetComment toDto(Comment comment){
            UserResponseDto.CommunityUserInfo userInfo = UserResponseDto.CommunityUserInfo.toDto(comment.getUser());

            return CommentResponseDto.GetComment.builder()
                    .uuid(comment.getUuid())
                    .content(comment.getContent())
                    .user(userInfo)
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class GetCommentList {

        private List<CommentResponseDto.GetComment> commentList;


        public static CommentResponseDto.GetCommentList toDto(List<Comment> comments){

            List<CommentResponseDto.GetComment> getComments = new ArrayList<>();

            for (Comment comment : comments) {
                //dto로 만들어 add
                getComments.add(CommentResponseDto.GetComment.toDto(comment));
            }

            return CommentResponseDto.GetCommentList.builder()
                    .commentList(getComments)
                    .build();
        }
    }
}
