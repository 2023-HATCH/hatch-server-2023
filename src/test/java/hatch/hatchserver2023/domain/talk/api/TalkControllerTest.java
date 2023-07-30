package hatch.hatchserver2023.domain.talk.api;

import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.TalkStatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.config.restdocs.RestDocsConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@WebMvcTest(controllers = {TalkController.class})
@MockBean(JpaMetamodelMappingContext.class)
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
class TalkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    private TalkService talkService;

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
    }


    @Test
    void getTalkMessages() throws Exception {
        //given
        int page = 0;
        int size = 3;
        ZonedDateTime now = ZonedDateTime.now();

        User sender = User.builder()
                .uuid(uuid)
                .profileImg(profileImg)
                .nickname(nickname)
                .build();
        TalkMessage talkMessage1 = TalkMessage.builder()
                .uuid(UUID.randomUUID())
//                .id(0L)
                .content("1 메세지내용 test")
                .user(sender)
                .build();
        TalkMessage talkMessage2 = TalkMessage.builder()
                .uuid(UUID.randomUUID())
//                .id(0L)
                .content("2 메세지내용 test")
                .user(sender)
                .build();
        talkMessage1.updateForTestCode(now);
        talkMessage2.updateForTestCode(now);

        List<TalkMessage> talkMessageList = List.of(talkMessage1, talkMessage2);
        Pageable pageable = PageRequest.of(page, size);

        Slice<TalkMessage> talkMessages = new SliceImpl<TalkMessage>(talkMessageList, pageable, false);


        //when
        when(talkService.getTalkMessages(page,size)).thenReturn(talkMessages);

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/talks/messages")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = TalkStatusCode.GET_TALK_MESSAGES_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.page").value(page)) //.toString()
                .andExpect(jsonPath("$.data.size").value(size))
                .andExpect(jsonPath("$.data.messages[0].messageId").value(talkMessage1.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[0].content").value(talkMessage1.getContent()))
                .andExpect(jsonPath("$.data.messages[0].sender.userId").value(sender.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[0].sender.nickname").value(sender.getNickname()))
                .andExpect(jsonPath("$.data.messages[0].sender.profileImg").value(sender.getProfileImg()))
                .andExpect(jsonPath("$.data.messages[1].messageId").value(talkMessage2.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[1].content").value(talkMessage2.getContent()))
                .andExpect(jsonPath("$.data.messages[1].sender.userId").value(sender.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[1].sender.nickname").value(sender.getNickname()))
                .andExpect(jsonPath("$.data.messages[1].sender.profileImg").value(sender.getProfileImg()))
        ;


        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("조회할 페이지 번호"),
                                        parameterWithName("size").description("조회할 한 페이지 크기")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("page").type("Integer").description("조회된 페이지 번호"),
                                        fieldWithPath("size").type("Integer").description("조회된 한 페이지 크기"),
                                        fieldWithPath("messages[].messageId").type("UUID").description("메세지 식별자"),
                                        fieldWithPath("messages[].createdAt").type("LocalDateTime").description("메세지 전송 시각"),
                                        fieldWithPath("messages[].content").type(JsonFieldType.STRING).description("메세지 내용"),
                                        fieldWithPath("messages[].sender.userId").type("UUID").description("메세지 전송자 식별자"),
                                        fieldWithPath("messages[].sender.nickname").type(JsonFieldType.STRING).description("메세지 전송자 닉네임"),
                                        fieldWithPath("messages[].sender.profileImg").type(JsonFieldType.STRING).description("메세지 전송자 프로필사진").optional()
                                )
                        )
                )
        ;
    }
}