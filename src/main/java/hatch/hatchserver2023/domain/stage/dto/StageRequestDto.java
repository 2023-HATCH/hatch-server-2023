package hatch.hatchserver2023.domain.stage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import hatch.hatchserver2023.domain.stage.SkeletonPoint;
import hatch.hatchserver2023.domain.stage.application.StageRoutineService;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StageRequestDto {

    @ToString
    @Getter
    @Builder
    public static class SendPlaySkeleton {
        @Max(StageRoutineService.STAGE_PLAYER_COUNT_VALUE -1)
        @PositiveOrZero
        @NotNull
        private Integer playerNum;

        @PositiveOrZero
        @NotNull
        private Integer frameNum;

//        @NotNull
        private Skeleton skeleton;
    }

    @ToString
    @Getter
    @Builder
    public static class Skeleton {
        @JsonProperty(SkeletonPoint.NOSE)
        private Point nose;

        @JsonProperty(SkeletonPoint.Left.EYE_INNER)
        private Point leftEyeInner;
        @JsonProperty(SkeletonPoint.Left.EYE)
        private Point leftEye;
        @JsonProperty(SkeletonPoint.Left.EYE_OUTER)
        private Point leftEyeOuter;
        @JsonProperty(SkeletonPoint.Right.EYE_INNER)
        private Point rightEyeInner;
        @JsonProperty(SkeletonPoint.Right.EYE)
        private Point rightEye;
        @JsonProperty(SkeletonPoint.Right.EYE_OUTER)
        private Point rightEyeOuter;

        @JsonProperty(SkeletonPoint.Left.EAR)
        private Point leftEar;
        @JsonProperty(SkeletonPoint.Right.EAR)
        private Point rightEar;

        @JsonProperty(SkeletonPoint.Left.MOUTH)
        private Point leftMouth;
        @JsonProperty(SkeletonPoint.Right.MOUTH)
        private Point rightMouth;

        @JsonProperty(SkeletonPoint.Left.SHOULDER)
        private Point leftShoulder;
        @JsonProperty(SkeletonPoint.Right.SHOULDER)
        private Point rightShoulder;

        @JsonProperty(SkeletonPoint.Left.ELBOW)
        private Point leftElbow;
        @JsonProperty(SkeletonPoint.Right.ELBOW)
        private Point rightElbow;

        @JsonProperty(SkeletonPoint.Left.WRIST)
        private Point leftWrist;
        @JsonProperty(SkeletonPoint.Right.WRIST)
        private Point rightWrist;

        @JsonProperty(SkeletonPoint.Left.PINKY)
        private Point leftPinky;
        @JsonProperty(SkeletonPoint.Right.PINKY)
        private Point rightPinky;

        @JsonProperty(SkeletonPoint.Left.INDEX)
        private Point leftIndex;
        @JsonProperty(SkeletonPoint.Right.INDEX)
        private Point rightIndex;

        @JsonProperty(SkeletonPoint.Left.THUMB)
        private Point leftThumb;
        @JsonProperty(SkeletonPoint.Right.THUMB)
        private Point rightThumb;

        @JsonProperty(SkeletonPoint.Left.HIP)
        private Point leftHip;
        @JsonProperty(SkeletonPoint.Right.HIP)
        private Point rightHip;

        @JsonProperty(SkeletonPoint.Left.KNEE)
        private Point leftKnee;
        @JsonProperty(SkeletonPoint.Right.KNEE)
        private Point rightKnee;

        @JsonProperty(SkeletonPoint.Left.ANKLE)
        private Point leftAnkle;
        @JsonProperty(SkeletonPoint.Right.ANKLE)
        private Point rightAnkle;

        @JsonProperty(SkeletonPoint.Left.HEEL)
        private Point leftHeel;
        @JsonProperty(SkeletonPoint.Right.HEEL)
        private Point rightHeel;

        @JsonProperty(SkeletonPoint.Left.FOOT_INDEX)
        private Point leftFootIndex;
        @JsonProperty(SkeletonPoint.Right.FOOT_INDEX)
        private Point rightFootIndex;

        public Float[] toAIFloatArray() {
            // TODO : null인 필드 에러 발생
            List<Point> AIPoints = List.of(nose, rightShoulder, rightElbow, rightWrist, leftShoulder, leftElbow, leftWrist, rightHip, rightKnee, rightAnkle, leftHip, leftKnee, leftAnkle);

            List<Float> values = new ArrayList<>();
            for(Point point : AIPoints) {
                try{
                    values.add(point.getX().floatValue());
                    values.add(point.getY().floatValue());
                }catch (Exception e) {
                    values.add(null); // 값을 못 얻어오면 null 추가
                }
            }

            return values.toArray(new Float[0]);
        }

        public static Float[][] toAIFloatArrays(List<Skeleton> skeletons) {
            List<Float[]> arrays = new ArrayList<>();
            for(Skeleton skeleton : skeletons) {
                arrays.add(skeleton.toAIFloatArray());
            }

            return arrays.toArray(new Float[0][0]);
        }
    }

    @ToString
    @Getter
    @Builder
    public static class Point {
        private Integer type;
        private Double x;
        private Double y;
        private Double z;
        private Double likelihood;
    }

    @ToString
    @Getter
    @Builder
    public static class SendMvpSkeleton {
        @PositiveOrZero
        @NotNull
        private Integer frameNum;

        //        @NotNull
        private Skeleton skeleton;
    }
}
