package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
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
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("Video Controller Unit Test")
public class VideoControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

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
    void setup(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();

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
            @DisplayName("For User and Anonymous")
            void getVideoDetail() throws Exception {
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
                        .andExpect(jsonPath("$.data.user.userId").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoUrl").value(video1.getVideoUrl()))
                        .andExpect(jsonPath("$.data.liked").value(isLiked))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                docs.document(
                                        pathParameters(
                                                parameterWithName("videoId").description("동영상 UUID")
                                        ),
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.").optional(),
                                                headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.").optional()
                                        ),
                                        responseFields( // response 필드 정보 입력
                                                beneathPath("data"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("createdAt").type("DateTime").description("생성 시각"),
                                                fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부")
                                        )
                                )
                        )
                ;
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
            void deleteVideo() throws Exception {
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
                                docs.document(
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
            @DisplayName("By createdAt desc")
            void getVideoListByCreatedAt() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);
                Slice<Video> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

                given(videoService.findByCreatedAt(any()))
                        .willReturn(slice);

                //when & then
                StatusCode code = VideoStatusCode.GET_VIDEO_LIST_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                        .get("/api/v1/videos")
                        .header("headerXAccessToken", "headerXAccessToken")
                        .header("headerXRefreshToken", "headerXRefreshToken")
                        .param("page", "0")
                        .param("size", "2");

                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.videoList[0].uuid").value(video1.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].uuid").value(video2.getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[0].title").value(video1.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[1].title").value(video2.getTitle()))
                        .andExpect(jsonPath("$.data.videoList[0].user.userId").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.userId").value(video2.getUserId().getUuid().toString()))
                        ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                docs.document(    //문서 조각 디렉토리 명
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional(),
                                                headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional()
                                        ),
                                        requestParameters(
                                                parameterWithName("page").description("페이지 번호(0부터 시작)"),
                                                parameterWithName("size").description("페이지 크기")
                                        ),
                                        //TODO: responseFields 두 개를 쓰면 바로 에러가 나는데... 이 출력이 최선이냐?
                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                                fieldWithPath("videoList.[].uuid").type(JsonFieldType.STRING).description("생성된 동영상 식별자 UUID"),
                                                fieldWithPath("videoList.[].title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("videoList.[].tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("videoList.[].user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("videoList.[].user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("videoList.[].user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("videoList.[].user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoList.[].videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("videoList.[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("videoList.[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("videoList.[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("videoList.[].length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("videoList.[].createdAt").type("DateTime").description("생성 시각"),
                                                fieldWithPath("videoList.[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                        )

                                )
                        );
            }


            @Test
            @DisplayName("By Random")
            void getVideoListByRandom() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);
                Slice<Video> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

                given(videoService.findByRandom(any()))
                        .willReturn(slice);

                //when
                StatusCode code = VideoStatusCode.GET_VIDEO_LIST_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                        .get("/api/v1/videos/random")
                        .header("headerXAccessToken", "headerXAccessToken")
                        .header("headerXRefreshToken", "headerXRefreshToken")
                        .param("page", "0")
                        .param("size", "2");

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
                        .andExpect(jsonPath("$.data.videoList[0].user.userId").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.userId").value(video2.getUserId().getUuid().toString()))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                docs.document(    //문서 조각 디렉토리 명
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional(),
                                                headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional()
                                        ),
                                        requestParameters(
                                                parameterWithName("page").description("페이지 번호(0부터 시작)"),
                                                parameterWithName("size").description("페이지 크기")
                                        ),
                                        //TODO: responseFields 두 개를 쓰면 바로 에러가 나는데... 이 출력이 최선이냐?
                                        responseFields(
                                                beneathPath("data"),
                                                fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                                fieldWithPath("videoList.[].uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("videoList.[].title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("videoList.[].tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("videoList.[].user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("videoList.[].user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("videoList.[].user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("videoList.[].user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoList.[].videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("videoList.[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("videoList.[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("videoList.[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("videoList.[].length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("videoList.[].createdAt").type("DateTime").description("생성 시각"),
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
            void postVideoUpload() throws Exception {
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
                                docs.document(
                                        requestParameters(
                                                parameterWithName("title").description("영상 제목"),
                                                parameterWithName("tag").description("영상 해시태그").optional(),
                                                parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                        ),
                                        requestParts(
                                                partWithName("video").description("Multipartfile의 동영상")
                                        ),
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
            void searchVideoListByHashtag() throws Exception {
                //given
                List<Video> videoList = Arrays.asList(video1, video2);
                Slice<Video> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

                given(hashtagService.searchHashtag(eq(tag), any(Pageable.class)))
                        .willReturn(slice);

                //when
                StatusCode code = VideoStatusCode.HASHTAG_SEARCH_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                                                            .get("/api/v1/videos/search")
                                                            .header("headerXAccessToken", "headerXAccessToken")
                                                            .header("headerXRefreshToken", "headerXRefreshToken")
                                                            .param("tag", tag)
                                                            .param("page", "0")
                                                            .param("size", "2");

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
                        .andExpect(jsonPath("$.data.videoList[0].user.userId").value(video1.getUserId().getUuid().toString()))
                        .andExpect(jsonPath("$.data.videoList[1].user.userId").value(video2.getUserId().getUuid().toString()))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                docs.document(
                                        requestHeaders(
                                                headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional(),
                                                headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n liked의 차이").optional()
                                        ),
                                        requestParameters(
                                                parameterWithName("tag").description("검색하고자 하는 해시태그"),
                                                parameterWithName("page").description("페이지 번호(0부터 시작)"),
                                                parameterWithName("size").description("페이지 크기")
                                        ),
                                        responseFields(
                                                beneathPath("data.videoList").withSubsectionId("beneath-data-video-list"),
                                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID"),
                                                fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                                fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                                fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                                fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                                fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                                fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로"),
                                                fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                                fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                                fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                                fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                                fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                                fieldWithPath("createdAt").type("DateTime").description("생성 시각"),
                                                fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                        )
                                )
                        );
            }
        }
    }

    @Nested
    @DisplayName("Get Hashtag List")
    class HashtagList {

        @Nested
        @DisplayName("Success")
        class SuccessCase {

            @Test
            @DisplayName("Hashtag List")
            void GetHashtagList() throws Exception {
                //given
                List<String> tagList = Arrays.asList("해시태그", "목록을", "이렇게", "준답니다", ";)");

                given(hashtagService.getHashtagList())
                        .willReturn(tagList);

                //when
                StatusCode code = VideoStatusCode.GET_HASHTAG_LIST_SUCCESS;

                MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                    .get("/api/v1/videos/tags");

                //then
                ResultActions resultActions = mockMvc.perform(requestGet);

                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(code.getCode()))
                        .andExpect(jsonPath("$.message").value(code.getMessage()))
                        .andExpect(jsonPath("$.data.tagList[0]").value(tagList.get(0)))
                        .andExpect(jsonPath("$.data.tagList[1]").value(tagList.get(1)))
                        .andExpect(jsonPath("$.data.tagList[2]").value(tagList.get(2)))
                        .andExpect(jsonPath("$.data.tagList[3]").value(tagList.get(3)))
                        .andExpect(jsonPath("$.data.tagList[4]").value(tagList.get(4)))
                ;

                resultActions
                        .andDo( //rest docs 문서 작성 시작
                                docs.document(
                                        responseFields(
                                                beneathPath("data").withSubsectionId("beneath-data"),
                                                fieldWithPath("tagList").type(JsonFieldType.ARRAY).description("해시태그 전체 목록")
                                        )
                                )
                        );
            }
        }
    }

}
