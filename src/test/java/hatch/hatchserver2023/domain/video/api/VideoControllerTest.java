package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.video.application.LikeService;
import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(VideoController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@DisplayName("Video Controller Unit Test")
public class VideoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    VideoService videoService;

    @MockBean
    HashtagService hashtagService;

    @MockBean
    LikeService likeService;

    private Video video1;
    private Video video2;
    private User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(998L)
                .uuid(UUID.randomUUID())
                .email("이메일")
                .nickname("닉네임")
                .followerCount(0)
                .followingCount(0)
                .instagramAccount("인스타 계정")
                .twitterAccount("트위터 계정")
                .kakaoAccountNumber(997L)
                .introduce("자기 소개 글")
                .profileImg("프로필 이미지 s3 경로")
                .build();

        video1 = Video.builder()
                .id(990L)
                .uuid(UUID.randomUUID())
                .title("타이틀")
                .tag("#해시 #태그")
                .userId(user)
                .videoUrl("동영상 s3 경로")
                .thumbnailUrl("썸네일 이미지 s3 경로")
                .likeCount(3)
                .commentCount(11)
                .length(107800)
                .build();

        video2 = Video.builder()
                .id(991L)
                .uuid(UUID.randomUUID())
                .title("타이틀 1")
                .tag("#해시 #태그 #1")
                .userId(user)
                .videoUrl("동영상 s3 경로 1")
                .thumbnailUrl("썸네일 이미지 s3 경로 1")
                .likeCount(5)
                .commentCount(2)
                .length(9999)
                .build();

    }

    @Nested
    @DisplayName("Get Video Detail")
    class GetVideoDetail {

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("For User")
            void getVideoDetailForUserSuccess() throws Exception {
                //given
                boolean isLiked = false;
                given(videoService.findOne(video1.getUuid()))
                        .willReturn(video1);

                given(likeService.isAlreadyLiked(video1, user))
                        .willReturn(isLiked);


                //when
                StatusCode code = VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                                                            .get("/api/v1/videos/{videoId}", video1.getUuid())
                                                            .header("headerXAccessToken", "headerXAccessToken")
                                                            .header("headerXRefreshToken", "headerXRefreshToken");

                //then
                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.uuid").value(video1.getUuid().toString()))
                        .andExpect(jsonPath("$.data.title").value(video1.getTitle()))
                        .andExpect(jsonPath("$.data.user.uuid").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoUrl").value(video1.getVideoUrl()))
                        .andExpect(jsonPath("$.data.liked").value(isLiked))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("get-video-detail-for-user",    //문서 조각 디렉토리 명
                                        pathParameters(
                                                parameterWithName("videoId").description("동영상 UUID")
                                        ),
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                                headerWithName("headerXRefreshToken").description("headerXRefreshToken")
                                        ),
                                        responseFields( // response 필드 정보 입력
                                                beneathPath("data"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("user.uuid").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("createdTime").type("DateTime").description("생성 시각"),
                                                fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부")
                                        )
                                )
                        );

            }

            @Test
            @DisplayName("For Anonymous")
            void getVideoDetailForAnonymousSuccess() throws Exception {
                //given
                boolean isLiked = false;
                given(videoService.findOne(video2.getUuid()))
                        .willReturn(video2);

                given(likeService.isAlreadyLiked(video2, user))
                        .willReturn(isLiked);


                //when & then
                StatusCode code = VideoStatusCode.GET_VIDEO_DETAIL_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                        .get("/api/v1/videos/anonymous/{videoId}", video2.getUuid());

                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.uuid").value(video2.getUuid().toString()))
                        .andExpect(jsonPath("$.data.title").value(video2.getTitle()))
                        .andExpect(jsonPath("$.data.user.uuid").value(video2.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoUrl").value(video2.getVideoUrl()))
                        .andExpect(jsonPath("$.data.liked").value(isLiked))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("get-video-detail-for-anonymous",    //문서 조각 디렉토리 명
                                        pathParameters(
                                                parameterWithName("videoId").description("동영상 UUID")
                                        ),

                                        responseFields( // response 필드 정보 입력
                                                beneathPath("data"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("user.uuid").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("createdTime").type("DateTime").description("생성 시각"),
                                                fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부 - 무조건 false")
                                        )
                                )
                        );

            }
        }
    }


    @Nested
    @DisplayName("Delete")
    class DeleteVideo {

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("Delete Success")
            void deleteVideoSuccess() throws Exception {
                //given
//                given(videoService.deleteOne(video1.getUuid()));

                //when
                StatusCode code = VideoStatusCode.VIDEO_DELETE_SUCCESS;

                MockHttpServletRequestBuilder requestDelete = RestDocumentationRequestBuilders
                        .delete("/api/v1/videos/{videoId}", video1.getUuid())
                        .with(csrf());

                ResultActions resultActions = mockMvc.perform(requestDelete);

                //then
                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.success").value(true))
                        ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("delete-video",
                                        pathParameters(
                                                parameterWithName("videoId").description("동영상 UUID")
                                        ),
                                        requestParameters(
                                                parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                        ),
                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (default: true)")
                                        )
                                )
                        );
            }
        }
    }

    @Nested
    @DisplayName("Get Video List")
    class GetVideoList {

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("By createdTime desc")
            void getVideoListByNewestSuccess() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);
                Slice<Video> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

                given(videoService.findByCreatedTime(any()))
                        .willReturn(slice);

                //when & then
                StatusCode code = VideoStatusCode.GET_VIDEO_LIST_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders.get("/api/v1/videos");

                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.videoList[0].uuid").value(video1.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].uuid").value(video2.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[0].title").value(video1.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[1].title").value(video2.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[0].user.uuid").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.uuid").value(video2.getUserId().getUuid().toString()))
                        ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("get-video-list-by-createdTime",    //문서 조각 디렉토리 명
                                        requestParameters(
                                                parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
                                                parameterWithName("size").description("페이지 크기").optional()
                                        ),
                                        //TODO: responseFields 두 개를 쓰면 바로 에러가 나는데... 이 출력이 최선이냐?
                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                                fieldWithPath("videoList.[].uuid").type(JsonFieldType.STRING).description("생성된 동영상 식별자 UUID"),
                                                fieldWithPath("videoList.[].title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("videoList.[].tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("videoList.[].user.uuid").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("videoList.[].user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("videoList.[].user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("videoList.[].user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoList.[].videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("videoList.[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("videoList.[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("videoList.[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("videoList.[].length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("videoList.[].createdTime").type("DateTime").description("생성 시각"),
                                                fieldWithPath("videoList.[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                        )

                                )
                        );
            }


            @Test
            @DisplayName("By Random")
            void getVideoListByRandomSuccess() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);
                Slice<Video> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

                given(videoService.findByRandom(any()))
                        .willReturn(slice);

                //when
                StatusCode code = VideoStatusCode.GET_VIDEO_LIST_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders.get("/api/v1/videos/random");

                //then
                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.videoList[0].uuid").value(video1.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].uuid").value(video2.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[0].title").value(video1.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[1].title").value(video2.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[0].user.uuid").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.uuid").value(video2.getUserId().getUuid().toString()))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("get-video-list-by-random",    //문서 조각 디렉토리 명
                                        requestParameters(
                                                parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
                                                parameterWithName("size").description("페이지 크기").optional()
                                        ),
                                        //TODO: responseFields 두 개를 쓰면 바로 에러가 나는데... 이 출력이 최선이냐?
                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                                fieldWithPath("videoList.[].uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("videoList.[].title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("videoList.[].tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("videoList.[].user.uuid").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("videoList.[].user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("videoList.[].user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("videoList.[].user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoList.[].videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("videoList.[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("videoList.[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("videoList.[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("videoList.[].length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("videoList.[].createdTime").type("DateTime").description("생성 시각"),
                                                fieldWithPath("videoList.[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                        )

                                )
                        );

            }


        }
    }

    @Nested
    @DisplayName("Upload")
    class VideoUpload {

        private String jsonFileName;

        @BeforeEach
        void setup() {
            jsonFileName = "video";
        }

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("Video Upload")
            void videoUploadSuccess() throws Exception {
                //given
                String insteadOfActualFile = "videoFile";
                MockMultipartFile mockMultipartFile = new MockMultipartFile(jsonFileName, jsonFileName, "application/json", insteadOfActualFile.getBytes(StandardCharsets.UTF_8));

                given(videoService.createVideo(any(MultipartFile.class), any(), eq(video1.getTitle()), eq(video1.getTag())))
                        .willReturn(video1);

                //when
                MockHttpServletRequestBuilder requestPost = RestDocumentationRequestBuilders
                                                        .multipart("/api/v1/videos")
                                                        .file(mockMultipartFile)
                                                        .param("title", video1.getTitle())
                                                        .param("tag", video1.getTag())
                                                        .header("headerXAccessToken", "headerXAccessToken")
                                                        .header("headerXRefreshToken", "headerXRefreshToken")
                                                        .contentType(APPLICATION_JSON)
                                                        .accept(APPLICATION_JSON)
                                                        .with(csrf());


                //then
                StatusCode code = VideoStatusCode.VIDEO_UPLOAD_SUCCESS;

                ResultActions resultActions = mockMvc.perform(requestPost);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.uuid").value(video1.getUuid().toString()))
                    ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("post-video-upload",
                                        requestParameters(
                                                parameterWithName("title").description("영상 제목"),
                                                parameterWithName("tag").description("영상 해시태그").optional(),
                                                parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                        ),
                                        //TODO: 토큰 이름이 맞는가?
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                                headerWithName("headerXRefreshToken").description("headerXAccessToken")
                                        ),

                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("생성된 동영상 식별자 UUID")
                                        )

                                )
                        );
            }
        }

    }


    @Nested
    @DisplayName("Video Search By Hashtag")
    class Search {

        String tag = "태그";

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("Search Success")
            void getVideoListBySearchUsingHashtagSuccess() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);

                given(hashtagService.searchHashtag(tag))
                        .willReturn(videoList);

                //when
                StatusCode code = VideoStatusCode.HASHTAG_SEARCH_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                                                            .get("/api/v1/videos/search")
                                                            .param("tag", tag);

                //then
                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.videoList[0].uuid").value(video1.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].uuid").value(video2.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[0].title").value(video1.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[1].title").value(video2.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[0].user.uuid").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.uuid").value(video2.getUserId().getUuid().toString()))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                document("search-video-list-by-hasthag",
                                        requestParameters(
                                                parameterWithName("tag").description("검색하고자 하는 해시태그")
//                                                parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
//                                                parameterWithName("size").description("페이지 크기").optional()
                                        ),
                                        responseFields(
                                                beneathPath("data.videoList"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("user.uuid").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("createdTime").type("DateTime").description("생성 시각"),
                                                fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                        )
                                )
                        );
            }
        }
    }

}
