package hatch.hatchserver2023.domain.video.api;


import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.application.HashtagService;
import hatch.hatchserver2023.domain.video.application.VideoService;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.response.code.CommonCode;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(VideoController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("Video Controller Unit Test")
public class VideoControllerViewCountTest {

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

    @MockBean
    VideoCacheUtil videoCacheUtil;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        log.info("set up");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();
    }


    @Test
//    @WithMockUser
    void addViewCount() throws Exception {
        //given
        User user = User.builder()
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
        UUID videoId = UUID.randomUUID();
        Video video = Video.builder()
                .id(990L)
                .uuid(videoId)
                .title("타이틀")
                .tag("#해시 #태그")
                .userId(user)
                .videoUrl("동영상 s3 경로")
                .thumbnailUrl("썸네일 이미지 s3 경로")
                .likeCount(3)
                .commentCount(11)
                .length(107800)
                .build();

        //when
        when(videoService.findOne(videoId)).thenReturn(video);
//        when(videoCacheUtil.addViewCount(any(Video.class)))

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/videos/"+videoId+"/view")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader());

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = VideoStatusCode.ADD_VIEW_COUNT_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andDo(print())
        ;

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값").optional(),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값. 이 요청은 토큰 둘다 안보내도 정상 동작합니다. 토큰을 보낸다면 갱신 로직이 적용됩니다.").optional()
                                )
                        )
                )
        ;
    }


}
