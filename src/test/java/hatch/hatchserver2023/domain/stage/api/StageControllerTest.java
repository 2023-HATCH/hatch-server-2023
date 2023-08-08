package hatch.hatchserver2023.domain.stage.api;

import hatch.hatchserver2023.domain.stage.application.StageRoutineService;
import hatch.hatchserver2023.domain.stage.application.StageService;
import hatch.hatchserver2023.domain.stage.application.StageSocketService;
import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StageStatusCode;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import hatch.hatchserver2023.global.config.security.WithCustomAuth;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = {StageController.class})
@MockBean(JpaMetamodelMappingContext.class)
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
class StageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    private StageService stageService;
    @MockBean
    private StageSocketService stageSocketService; // 삭제
    @MockBean
    private TalkService talkService;
    @MockBean
    private UserUtilService userUtilService;

    private User user;

    private UUID uuid;
    private String nickname;
    private String profileImg;

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

        log.info("set up");
        uuid = UUID.randomUUID();
        nickname = "nicknameTest";
        profileImg = "http://testurl";

        user = User.builder()
                .uuid(uuid)
                .profileImg(profileImg)
                .nickname(nickname)
                .build();
    }


    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") //내가 만든 시큐리티 인증정보 주입 어노테이션. @AuthenticationPrincipal 처리
    @Test
    void enterStage() throws Exception {
        //given
        int userCount = 1;
        String stageStatus = StageRoutineService.STAGE_STATUS_WAIT;

        int page = 0;
        int size = 3;
        TalkMessage talkMessage1 = makeDummyTalkMessage(1);
        TalkMessage talkMessage2 = makeDummyTalkMessage(2);
        Slice<TalkMessage> talkMessages = makeDummyTalkMessages(talkMessage1, talkMessage2, page, size);

        //when
        when(stageService.addStageUser(any(User.class))).thenReturn(userCount);
        when(stageService.getStageInfo()).thenReturn(stageStatus);
        when(talkService.getTalkMessages(page, size)).thenReturn(talkMessages);

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/stage/enter")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader());

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = StageStatusCode.GET_STAGE_ENTER_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.userCount").value(userCount))
                .andExpect(jsonPath("$.data.stageStatus").value(stageStatus))
                .andExpect(jsonPath("$.data.talkMessageData.page").value(page))
                .andExpect(jsonPath("$.data.talkMessageData.size").value(size))
                .andExpect(jsonPath("$.data.talkMessageData.messages[0].messageId").value(talkMessage1.getUuid().toString()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[0].content").value(talkMessage1.getContent()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[0].sender.userId").value(user.getUuid().toString()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[0].sender.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[0].sender.profileImg").value(user.getProfileImg()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[1].messageId").value(talkMessage2.getUuid().toString()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[1].content").value(talkMessage2.getContent()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[1].sender.userId").value(user.getUuid().toString()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[1].sender.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.talkMessageData.messages[1].sender.profileImg").value(user.getProfileImg()))
        ;

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값").optional()
                                ),
                                requestParameters(
                                        parameterWithName("page").description("조회할 라이브톡 메세지 목록 페이지 번호"),
                                        parameterWithName("size").description("조회할 라이브톡 메세지 목록 한 페이지 크기")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("userCount").type("Integer").description("스테이지 내 사용자 수"),
                                        fieldWithPath("stageStatus").type(JsonFieldType.STRING).description("스테이지 현재 상태. WAIT, CATCH, PLAY, MVP 중 하나"),
                                        fieldWithPath("talkMessageData.page").type("Integer").description("조회된 라이브톡 메세지 목록 페이지 번호"),
                                        fieldWithPath("talkMessageData.size").type("Integer").description("조회된 라이브톡 메세지 목록 한 페이지 크기"),
                                        fieldWithPath("talkMessageData.messages[].messageId").type("UUID").description("메세지 식별자"),
                                        fieldWithPath("talkMessageData.messages[].createdAt").type("LocalDateTime").description("메세지 전송 시각"),
                                        fieldWithPath("talkMessageData.messages[].content").type(JsonFieldType.STRING).description("메세지 내용"),
                                        fieldWithPath("talkMessageData.messages[].sender.userId").type("UUID").description("메세지 전송자 식별자"),
                                        fieldWithPath("talkMessageData.messages[].sender.nickname").type(JsonFieldType.STRING).description("메세지 전송자 닉네임"),
                                        fieldWithPath("talkMessageData.messages[].sender.profileImg").type(JsonFieldType.STRING).description("메세지 전송자 프로필사진").optional()
                                )
                        )
                )
        ;

    }

    private TalkMessage makeDummyTalkMessage(int num) {
        ZonedDateTime now = ZonedDateTime.now();
        TalkMessage talkMessage = TalkMessage.builder()
                .uuid(UUID.randomUUID())
                .content(num+" 메세지내용 test")
                .user(user)
                .build();
        talkMessage.updateForTestCode(now);
        return talkMessage;
    }

    private Slice<TalkMessage> makeDummyTalkMessages(TalkMessage talkMessage1, TalkMessage talkMessage2, int page, int size) {
        List<TalkMessage> talkMessageList = List.of(talkMessage1, talkMessage2);
        Pageable pageable = PageRequest.of(page, size);
        return new SliceImpl<TalkMessage>(talkMessageList, pageable, false);
    }


    @Test
    void getStageUsers() throws Exception {
        //given
        List<Long> userIds = List.of(1L, 2L, 3L);
        User user1 = makeDummyUser(userIds.get(0).intValue());
        User user2 = makeDummyUser(userIds.get(1).intValue());
        User user3 = makeDummyUser(userIds.get(2).intValue());
        List<User> users = List.of(user1, user2, user3);

        //when
        when(stageService.getStageEnterUserIds()).thenReturn(userIds);
        when(userUtilService.getUsersById(userIds)).thenReturn(users);

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/stage/users")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader());

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = StageStatusCode.GET_STAGE_ENTER_USER_LIST_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.[0].userId").value(uuid.toString()))
                .andExpect(jsonPath("$.data.[0].nickname").value(nickname+1))
                .andExpect(jsonPath("$.data.[0].profileImg").value(profileImg+1))
                .andExpect(jsonPath("$.data.[1].userId").value(uuid.toString()))
                .andExpect(jsonPath("$.data.[1].nickname").value(nickname+2))
                .andExpect(jsonPath("$.data.[1].profileImg").value(profileImg+2))
                .andExpect(jsonPath("$.data.[2].userId").value(uuid.toString()))
                .andExpect(jsonPath("$.data.[2].nickname").value(nickname+3))
                .andExpect(jsonPath("$.data.[2].profileImg").value(profileImg+3))
        ;

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값").optional()
                                ),
                                responseFields(
                                        beneathPath("data"), // []
                                        fieldWithPath("userId").type("UUID").description("사용자 식별자"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 url").optional()
                                )
                        )
                )
        ;


    }

    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") // @AuthenticationPrincipal 처리
    @Test
    void registerCatch() throws Exception {
        //given
        //when

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/stage/catch")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader());

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = StageStatusCode.GET_CATCH_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()));

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값").optional()
                                )
                        )
                )
        ;

    }

    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") // @AuthenticationPrincipal 처리
    @Test
    void exitStage() throws Exception {
        //given
        //when

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/stage/exit")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader());

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = StageStatusCode.GET_STAGE_EXIT_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()));

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값").optional()
                                )
                        )
                )
        ;
    }



    private User makeDummyUser(int num) {
        return User.builder()
                .uuid(uuid)
                .nickname(nickname+num)
                .profileImg(profileImg+num)
                .build();
    }
}