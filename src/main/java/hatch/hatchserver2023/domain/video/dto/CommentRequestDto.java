package hatch.hatchserver2023.domain.video.dto;

import lombok.Getter;

@Getter
public class CommentRequestDto {

    private String content;

    public CommentRequestDto(String content){
        this.content = content;
    }

    public CommentRequestDto() {}
}
