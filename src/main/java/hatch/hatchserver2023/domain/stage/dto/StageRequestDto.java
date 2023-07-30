package hatch.hatchserver2023.domain.stage.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

public class StageRequestDto {

    @ToString
    @Getter
    @Builder
    public static class SendPlaySkeleton {
        private Integer playerNum;
        private Skeleton skeleton;
    }

    @ToString
    @Getter
    @Builder
    public static class Skeleton {
        private Point nose;
        private BodyPart right;
        private BodyPart left;
    }

    @ToString
    @Getter
    @Builder
    public static class BodyPart {
        private Point shoulder;
        private Point elbow;
        private Point wrist;
        private Point hip;
        private Point knee;
        private Point ankle;
    }

    @ToString
    @Getter
    @Builder
    public static class Point {
        private Double x;
        private Double y;
    }
}
