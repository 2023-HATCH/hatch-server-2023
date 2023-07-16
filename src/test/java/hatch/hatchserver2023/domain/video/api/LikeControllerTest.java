package hatch.hatchserver2023.domain.video.api;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.application.LikeService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@DisplayName("Like Controller Unit Test")
public class LikeControllerTest {

    @Autowired
    MockMvc mockMvc;

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


    // 좋아요 등록
    @Test
    @DisplayName("Add Like")
    void postAddLike() throws Exception {

        //given
        given(likeService.addLike(eq(video1.getUuid()), any()))
                .willReturn(video1.getUuid());

        //when
        StatusCode code = VideoStatusCode.LIKE_ADD_SUCCESS;

        MockHttpServletRequestBuilder requestPost = RestDocumentationRequestBuilders
                .post("/api/v1/likes/{videoId}", video1.getUuid())
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken")
                .with(csrf());

        //then
        ResultActions resultActions = mockMvc.perform(requestPost);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true))
        ;

        resultActions
                .andDo( //rest docs 문서 작성 시작
                        document("add-like",    //문서 조각 디렉토리 명
                                pathParameters(
                                        parameterWithName("videoId").description("동영상 UUID")
                                ),
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                        headerWithName("headerXRefreshToken").description("headerXRefreshToken")
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("좋아요 추가 성공 여부 (default: true)")
                                        )
                        )
                )
        ;
    }



    // 좋아요 삭제
    @Test
    @DisplayName("Delete Like")
    void deleteLike() throws Exception {

        //given
//        given(likeService.deleteLike(eq(video.getUuid()), any(User.class)));

        //when
        StatusCode code = VideoStatusCode.LIKE_DELETE_SUCCESS;

        MockHttpServletRequestBuilder requestDelete = RestDocumentationRequestBuilders
                .delete("/api/v1/likes/{videoId}", video1.getUuid())
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken")
                .with(csrf());

        //then
        ResultActions resultActions = mockMvc.perform(requestDelete);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true))
        ;

        resultActions
                .andDo(
                        document("delete-like",
                                pathParameters(
                                        parameterWithName("videoId").description("동영상 UUID")
                                ),
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                        headerWithName("headerXRefreshToken").description("headerXRefreshToken")
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("좋아요 삭제 성공 여부 (default: true)")
                                )
                        )
                )
        ;

    }


    // 사용자가 좋아요 누른 영상 목록 조회
    @Test
    @DisplayName("Get Liked Video List")
    void getLikedVideoList() throws Exception {
        //given
        List<Video> videoList = Arrays.asList(video1, video2);

        given(likeService.getLikedVideoList(any()))
                .willReturn(videoList);

        //when
        StatusCode code = VideoStatusCode.GET_LIKE_VIDEO_LIST_SUCCESS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/likes")
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken");

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
                .andDo(
                        document("get-liked-video-list",
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                        headerWithName("headerXRefreshToken").description("headerXRefreshToken")
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
                )
        ;

    }

}
