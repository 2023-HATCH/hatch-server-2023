package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class StageSocketResponseDto {

    @ToString
    @Getter
    @Builder
    public static class UserCount {
        private Integer userCount;
    }
}
