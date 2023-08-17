package hatch.hatchserver2023.domain.comment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.comment.application.CommentService;
import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.comment.dto.CommentRequestDto;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CommentController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("Comment Controller Unit Test")
public class CommentControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    CommentService commentService;

    private Video video;
    private User user;
    private Comment comment1;
    private Comment comment2;

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

        video = Video.builder()
                .id(990L)
                .uuid(UUID.randomUUID())
                .title("타이틀")
                .tag("#해시 #태그")
                .user(user)
                .videoUrl("동영상 s3 경로")
                .thumbnailUrl("썸네일 이미지 s3 경로")
                .likeCount(3)
                .commentCount(11)
                .length(107800)
                .build();

        comment1 = Comment.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .video(video)
                .content("댓글을 작성했어요!")
                .build();

        comment2 = Comment.builder()
                .uuid(UUID.randomUUID())
                .user(user)
                .video(video)
                .content("또 다른 댓글을 썼습니다")
                .build();
    }


    @Test
    @DisplayName("Add Comment")
    void postAddComment() throws Exception {
        //given
        ObjectMapper objectMapper = new ObjectMapper();

        String content = comment1.getContent();

        given(commentService.createComment(eq(content), eq(video.getUuid()), any()))
                .willReturn(comment1);

        CommentRequestDto requestDto = new CommentRequestDto(content);

        //when
        StatusCode code = VideoStatusCode.COMMENT_REGISTER_SUCCESS;

        MockHttpServletRequestBuilder requestPost = RestDocumentationRequestBuilders
                .post("/api/v1/comments/{videoId}", video.getUuid())
                .content(objectMapper.writeValueAsString(requestDto))
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .with(csrf());

        //then
        ResultActions resultActions = mockMvc.perform(requestPost);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.uuid").value(comment1.getUuid().toString()))
                .andExpect(jsonPath("$.data.content").value(comment1.getContent()))
                .andExpect(jsonPath("$.data.user.userId").value(comment1.getUser().getUuid().toString()))
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
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                        headerWithName("headerXRefreshToken").description("headerXRefreshToken")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("uuid").type(JsonFieldType.STRING).description("생성된 댓글 UUID 식별자"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용"),
                                        fieldWithPath("user.userId").type(JsonFieldType.STRING).description("댓글 작성자 UUID 식별자"),
                                        fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("댓글 작성자 닉네임"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("댓글 작성자 이메일"),
                                        fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("댓글 작성자 프로필 사진 S3 경로")
                                )
                        )
                )
        ;
    }


    @Test
    @DisplayName("Delete Comment")
    void deleteComment() throws Exception {
        //given
//        given(commentService.deleteComment(comment1.getUuid(), user));

        //when
        StatusCode code = VideoStatusCode.COMMENT_DELETE_SUCCESS;

        MockHttpServletRequestBuilder requestDelete = RestDocumentationRequestBuilders
                .delete("/api/v1/comments/{commentId}", comment1.getUuid())
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
                        docs.document(
                                pathParameters(
                                        parameterWithName("commentId").description("삭제하려는 댓글 UUID 식별자")
                                ),
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("headerXAccessToken"),
                                        headerWithName("headerXRefreshToken").description("headerXRefreshToken")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("댓글 삭제 성공 여부 (default: true)")
                                )
                        )
                )
        ;
    }


    @Test
    @DisplayName("Get Comment List From Video")
    void getCommentListFromVideo() throws Exception {
        //given
        List<Comment> commentList = Arrays.asList(comment1, comment2);

        given(commentService.getCommentList(video.getUuid()))
                .willReturn(commentList);

        //when
        StatusCode code = VideoStatusCode.GET_COMMENT_LIST_SUCCESS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/comments/{videoId}", video.getUuid());

        //then
        ResultActions resultActions = mockMvc.perform(requestGet);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.commentList[0].uuid").value(comment1.getUuid().toString()))
                .andExpect(jsonPath("$.data.commentList[1].uuid").value(comment2.getUuid().toString()))
                .andExpect(jsonPath("$.data.commentList[0].content").value(comment1.getContent()))
                .andExpect(jsonPath("$.data.commentList[1].content").value(comment2.getContent()))
                .andExpect(jsonPath("$.data.commentList[0].user.userId").value(comment1.getUser().getUuid().toString()))
                .andExpect(jsonPath("$.data.commentList[1].user.userId").value(comment2.getUser().getUuid().toString()))
        ;

        resultActions
                .andDo(
                        docs.document(
                                pathParameters(
                                        parameterWithName("videoId").description("동영상 UUID")
                                ),
                                responseFields(
                                        beneathPath("data.commentList").withSubsectionId("beneath-data-comment-list"),
                                        fieldWithPath("uuid").type(JsonFieldType.STRING).description("생성된 댓글 UUID 식별자"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("댓글 내용"),
                                        fieldWithPath("user.userId").type(JsonFieldType.STRING).description("댓글 작성자 UUID 식별자"),
                                        fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("댓글 작성자 닉네임"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("댓글 작성자 이메일"),
                                        fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("댓글 작성자 프로필 사진 S3 경로"),
                                        fieldWithPath("createdAt").type("DateTime").description("댓글 생성 시각")
                                )
                        )
                );

    }

}
