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

        @NotNull
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

/*        public Float[] toFloatArray() {
            List<Float> values = new ArrayList<>();
            for(Field field : this.getClass().getDeclaredFields()) { //모든 필드명 얻기
                try{
                    values.add(Float.parseFloat(field.get(this).toString())); // 이 객체의 해당 필드값 얻기
                }catch (IllegalAccessException e) {
                    values.add(null); // 값을 못 얻어오면 null 추가
                }
            }

            return values.toArray(new Float[0]);
        }

        public static Float[][] toFloatArrays(List<Skeleton> skeletons) {
            List<Float[]> arrays = new ArrayList<>();
            for(Skeleton skeleton : skeletons) {
                arrays.add(skeleton.toFloatArray());
            }

            return arrays.toArray(new Float[0][0]);
        }*/
    }

    @ToString
    @Getter
    @Builder
    public static class Point {
        private String type;
        private Double x;
        private Double y;
        private Double z;
        private Double likelihood;
    }


// 구 Skeleton
//    @ToString
//    @Getter
//    @Builder
//    public static class Skeleton {
//        private Double noseX;
//        private Double noseY;
//        private Double noseZ;
//
//        private Double rightShoulderX;
//        private Double rightShoulderY;
//        private Double rightShoulderZ;
//        private Double rightElbowX;
//        private Double rightElbowY;
//        private Double rightElbowZ;
//        private Double rightWristX;
//        private Double rightWristY;
//        private Double rightWristZ;
//        private Double rightHipX;
//        private Double rightHipY;
//        private Double rightHipZ;
//        private Double rightKneeX;
//        private Double rightKneeY;
//        private Double rightKneeZ;
//        private Double rightAnkleX;
//        private Double rightAnkleY;
//        private Double rightAnkleZ;
//
//        private Double leftShoulderX;
//        private Double leftShoulderY;
//        private Double leftShoulderZ;
//        private Double leftElbowX;
//        private Double leftElbowY;
//        private Double leftElbowZ;
//        private Double leftWristX;
//        private Double leftWristY;
//        private Double leftWristZ;
//        private Double leftHipX;
//        private Double leftHipY;
//        private Double leftHipZ;
//        private Double leftKneeX;
//        private Double leftKneeY;
//        private Double leftKneeZ;
//        private Double leftAnkleX;
//        private Double leftAnkleY;
//        private Double leftAnkleZ;
//
//        public Float[] toFloatArray() {
//            List<Float> values = new ArrayList<>();
//            for(Field field : this.getClass().getDeclaredFields()) { //모든 필드명 얻기
//                try{
//                    values.add(Float.parseFloat(field.get(this).toString())); // 이 객체의 해당 필드값 얻기
//                }catch (IllegalAccessException e) {
//                    values.add(null); // 값을 못 얻어오면 null 추가
//                }
//            }
//
//            return values.toArray(new Float[0]);
//        }
//
//        public static Float[][] toFloatArrays(List<Skeleton> skeletons) {
//            List<Float[]> arrays = new ArrayList<>();
//            for(Skeleton skeleton : skeletons) {
//                arrays.add(skeleton.toFloatArray());
//            }
//
//            return arrays.toArray(new Float[0][0]);
//        }
//    }

// 플레이 종료 api 가 필요 없게 됨
//    @ToString
//    @Getter
//    @Builder
//    public static class EndPlay {
//        @NotNull
//        private String title;
//
//        @NotNull
//        private List<StageRequestDto.Skeleton> skeletons;
//    }


//
//    @ToString
//    @Getter
//    @Builder
//    public static class Skeleton {
//        private Point nose;
//        private BodyPart right;
//        private BodyPart left;
//    }
//
//    @ToString
//    @Getter
//    @Builder
//    public static class BodyPart {
//        private Point shoulder;
//        private Point elbow;
//        private Point wrist;
//        private Point hip;
//        private Point knee;
//        private Point ankle;
//    }
//
//    @ToString
//    @Getter
//    @Builder
//    public static class Point {
//        private Double x;
//        private Double y;
//    }
}
