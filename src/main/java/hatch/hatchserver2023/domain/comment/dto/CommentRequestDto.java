package hatch.hatchserver2023.domain.comment.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class CommentRequestDto {

    @NotBlank
    private String content;

    public CommentRequestDto(String content){
        this.content = content;
    }

    public CommentRequestDto() {}
}
