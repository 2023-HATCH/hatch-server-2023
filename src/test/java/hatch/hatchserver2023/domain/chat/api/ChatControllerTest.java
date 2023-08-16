package hatch.hatchserver2023.domain.chat.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.chat.application.ChatService;
import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.dto.ChatModel;
import hatch.hatchserver2023.domain.chat.dto.ChatRequestDto;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static hatch.hatchserver2023.global.config.restdocs.RestDocsConfig.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = {ChatController.class})
@MockBean(JpaMetamodelMappingContext.class)
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    private ChatService chatService;

    @MockBean
    private UserUtilService userUtilService;

    private UUID userId1;
    private User user1;
    private String nickname1;
    private String profileImg1;

    private UUID userId2;
    private User user2;
    private String nickname2;
    private String profileImg2;

    private UUID chatRoomId1;
    private UUID chatRoomId2;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        log.info("set up");

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();

        userId1 = UUID.randomUUID();
        nickname1 = "스펀지밥";
        profileImg1 = "http://testurl";

        userId2 = UUID.randomUUID();
        nickname2 = "뚱이";
        profileImg2 = "http://testurl2";

        chatRoomId1 = UUID.randomUUID();
        chatRoomId2 = UUID.randomUUID();

        user1 = User.builder()
                .uuid(userId1)
                .profileImg(profileImg1)
                .nickname(nickname1)
                .build();
        user2 = User.builder()
                .uuid(userId2)
                .profileImg(profileImg2)
                .nickname(nickname2)
                .build();

        log.info("set up end");
    }

    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") // @AuthenticationPrincipal 처리
    @Test
    void enterChatRoom() throws Exception {
        //given
        //요청 데이터
        int size = 5; // 이전 채팅 메세지 조회할 개수
        UUID opponentUserId = UUID.randomUUID();
        ChatRequestDto.CreateChatRoom requestDto = ChatRequestDto.CreateChatRoom.builder()
                .opponentUserId(String.valueOf(opponentUserId))
                .build();
        String requestDtoString = new ObjectMapper().writeValueAsString(requestDto);

        //응답될 채팅 메세지 데이터
        String chatContent1 = "안녕하세요~ 11";
        String chatContent2 = "아이스크림 먹자!!!!! 22";
        ZonedDateTime recentSendAt1 = ZonedDateTime.now();
        ZonedDateTime recentSendAt2 = ZonedDateTime.now();
        ChatRoom chatRoom1 = ChatRoom.builder()
                .uuid(chatRoomId1)
                .recentContent(chatContent1)
                .recentSendAt(recentSendAt1)
                .build();
        ChatMessage chatMessage1 = ChatMessage.builder()
                .uuid(UUID.randomUUID())
                .chatRoom(chatRoom1)
                .sender(user1)
                .content(chatContent1)
                .build();
        ChatMessage chatMessage2 = ChatMessage.builder()
                .uuid(UUID.randomUUID())
                .chatRoom(chatRoom1)
                .sender(user2)
                .content(chatContent2)
                .build();
        chatMessage1.updateForTestCode(recentSendAt1);
        chatMessage2.updateForTestCode(recentSendAt2);
        List<ChatMessage> chatMessageList = List.of(chatMessage1, chatMessage2);

        Pageable pageable = PageRequest.of(0, size);
        Slice<ChatMessage> chatMessages = new SliceImpl<ChatMessage>(chatMessageList, pageable, false);
        ChatModel.EnterChatRoom model = ChatModel.EnterChatRoom.toModel(chatRoom1.getUuid(), chatMessages);


        //when
        when(userUtilService.findOneByUuid(opponentUserId)).thenReturn(user1);
        when(chatService.enterChatRoom(any(User.class), any(User.class), eq(size))).thenReturn(model);

        //then
        MockHttpServletRequestBuilder requestPut = put("/api/v1/chats/rooms")
                .content(requestDtoString)
                .param("size", String.valueOf(size))
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestPut);

        StatusCode code = ChatStatusCode.PUT_ENTER_CHAT_ROOM_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.chatRoomId").value(chatRoomId1.toString()))
                .andExpect(jsonPath("$.data.recentMessages.page").value(0))
                .andExpect(jsonPath("$.data.recentMessages.size").value(size))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].chatMessageId").value(chatMessageList.get(0).getUuid().toString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].createdAt").value(chatMessageList.get(0).getCreatedAtString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].content").value(chatMessageList.get(0).getContent()))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].sender.userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].sender.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("$.data.recentMessages.messages[0].sender.profileImg").value(user1.getProfileImg()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].chatMessageId").value(chatMessageList.get(1).getUuid().toString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].createdAt").value(chatMessageList.get(1).getCreatedAtString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].content").value(chatMessageList.get(1).getContent()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].sender.userId").value(user2.getUuid().toString()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].sender.nickname").value(user2.getNickname()))
                .andExpect(jsonPath("$.data.recentMessages.messages[1].sender.profileImg").value(user2.getProfileImg()))
                .andDo(print())
        ;

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값")
                                ),
                                requestFields(
                                        fieldWithPath("opponentUserId").type("UUID").description("채팅 상대방 유저 식별자").attributes(field("constraints", "공백 불가"))
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("chatRoomId").type("UUID").description("생성된 채팅방 식별자"),
                                        fieldWithPath("recentMessages.page").type("Integer").description("조회된 페이지 번호"),
                                        fieldWithPath("recentMessages.size").type("Integer").description("조회된 한 페이지 크기"),
                                        fieldWithPath("recentMessages.messages[].chatMessageId").type("UUID").description("채팅 메세지 식별자"),
                                        fieldWithPath("recentMessages.messages[].createdAt").type("LocalDateTime").description("메세지 전송 시각"),
                                        fieldWithPath("recentMessages.messages[].content").type(JsonFieldType.STRING).description("메세지 내용"),
                                        fieldWithPath("recentMessages.messages[].sender.userId").type("UUID").description("메세지 전송자 식별자"),
                                        fieldWithPath("recentMessages.messages[].sender.nickname").type(JsonFieldType.STRING).description("메세지 전송자 닉네임"),
                                        fieldWithPath("recentMessages.messages[].sender.profileImg").type(JsonFieldType.STRING).description("메세지 전송자 프로필사진").optional()
                                )
                        )
                )
        ;
    }

    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") // @AuthenticationPrincipal 처리
    @Test
    void getChatRooms() throws Exception {
        //given
        String recentContent1 = "안녕하세요~ 11";
        String recentContent2 = "아이스크림 먹자!!!!! 22";
        ZonedDateTime recentSendAt1 = ZonedDateTime.now(); //.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
        ZonedDateTime recentSendAt2 = ZonedDateTime.now();
        ChatRoom chatRoom1 = ChatRoom.builder()
                .uuid(chatRoomId1)
                .recentContent(recentContent1)
                .recentSendAt(recentSendAt1)
                .build();
        ChatRoom chatRoom2 = ChatRoom.builder()
                .uuid(chatRoomId2)
                .recentContent(recentContent2)
                .recentSendAt(recentSendAt2)
                .build();
        ChatModel.ChatRoomInfo chatRoomInfo1 = ChatModel.ChatRoomInfo.builder()
                .opponentUser(user1)
                .chatRoom(chatRoom1)
                .build();
        ChatModel.ChatRoomInfo chatRoomInfo2 = ChatModel.ChatRoomInfo.builder()
                .opponentUser(user2)
                .chatRoom(chatRoom2)
                .build();
        List<ChatModel.ChatRoomInfo> chatRoomInfos = List.of(chatRoomInfo1, chatRoomInfo2);

        //when
        when(chatService.getChatRoomInfos(any(User.class))).thenReturn(chatRoomInfos);

        //then
        MockHttpServletRequestBuilder requestGet = get("/api/v1/chats/rooms")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = ChatStatusCode.GET_CHAT_ROOMS_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.chatRooms[0].chatRoomId").value(chatRoomInfos.get(0).getChatRoom().getUuid().toString()))
                .andExpect(jsonPath("$.data.chatRooms[0].opponentUser.userId").value(chatRoomInfos.get(0).getOpponentUser().getUuid().toString()))
                .andExpect(jsonPath("$.data.chatRooms[0].opponentUser.nickname").value(chatRoomInfos.get(0).getOpponentUser().getNickname()))
                .andExpect(jsonPath("$.data.chatRooms[0].opponentUser.profileImg").value(chatRoomInfos.get(0).getOpponentUser().getProfileImg()))
                .andExpect(jsonPath("$.data.chatRooms[0].recentContent").value(chatRoomInfos.get(0).getChatRoom().getRecentContent()))
                .andExpect(jsonPath("$.data.chatRooms[0].recentSendAt").value(chatRoomInfos.get(0).getChatRoom().getRecentSendAtString()))
                .andExpect(jsonPath("$.data.chatRooms[1].chatRoomId").value(chatRoomInfos.get(1).getChatRoom().getUuid().toString()))
                .andExpect(jsonPath("$.data.chatRooms[1].opponentUser.userId").value(chatRoomInfos.get(1).getOpponentUser().getUuid().toString()))
                .andExpect(jsonPath("$.data.chatRooms[1].opponentUser.nickname").value(chatRoomInfos.get(1).getOpponentUser().getNickname()))
                .andExpect(jsonPath("$.data.chatRooms[1].opponentUser.profileImg").value(chatRoomInfos.get(1).getOpponentUser().getProfileImg()))
                .andExpect(jsonPath("$.data.chatRooms[1].recentContent").value(chatRoomInfos.get(1).getChatRoom().getRecentContent()))
                .andExpect(jsonPath("$.data.chatRooms[1].recentSendAt").value(chatRoomInfos.get(1).getChatRoom().getRecentSendAtString()))
                .andDo(print())
        ;

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("chatRooms[].chatRoomId").type("UUID").description("채팅방 식별자"),
                                        fieldWithPath("chatRooms[].opponentUser").type("-").description("채팅 상대 사용자 정보"),
                                        fieldWithPath("chatRooms[].opponentUser.userId").type("UUID").description("사용자 식별자"),
                                        fieldWithPath("chatRooms[].opponentUser.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                        fieldWithPath("chatRooms[].opponentUser.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 url").optional(),
                                        fieldWithPath("chatRooms[].recentContent").type(JsonFieldType.STRING).description("최근 전송된 메세지 내용").optional(),
                                        fieldWithPath("chatRooms[].recentSendAt").type("LocalDateTime").description("최근 메세지 전송 시각").optional()
                                )
                        )
                )
        ;
    }

    @Test
    void getChatMessages() throws Exception {
        //given
        int page = 0;
        int size = 5;
        String chatContent1 = "안녕하세요~ 11";
        String chatContent2 = "아이스크림 먹자!!!!! 22";
        ZonedDateTime recentSendAt1 = ZonedDateTime.now();
        ZonedDateTime recentSendAt2 = ZonedDateTime.now();
        ChatRoom chatRoom1 = ChatRoom.builder()
                .uuid(chatRoomId1)
                .recentContent(chatContent1)
                .recentSendAt(recentSendAt1)
                .build();
        ChatMessage chatMessage1 = ChatMessage.builder()
                .uuid(UUID.randomUUID())
                .chatRoom(chatRoom1)
                .sender(user1)
                .content(chatContent1)
                .build();
        ChatMessage chatMessage2 = ChatMessage.builder()
                .uuid(UUID.randomUUID())
                .chatRoom(chatRoom1)
                .sender(user2)
                .content(chatContent2)
                .build();
        chatMessage1.updateForTestCode(recentSendAt1);
        chatMessage2.updateForTestCode(recentSendAt2);
        List<ChatMessage> chatMessageList = List.of(chatMessage1, chatMessage2);

        Pageable pageable = PageRequest.of(page, size);
        Slice<ChatMessage> chatMessages = new SliceImpl<ChatMessage>(chatMessageList, pageable, false);


        //when
        when(chatService.getChatMessages(chatRoomId1, page, size)).thenReturn(chatMessages);

        //then
        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders.get("/api/v1/chats/rooms/{chatRoomId}/messages", chatRoomId1.toString())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestGet);

        StatusCode code = ChatStatusCode.GET_CHAT_MESSAGES_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.page").value(page))
                .andExpect(jsonPath("$.data.size").value(size))
                .andExpect(jsonPath("$.data.messages[0].chatMessageId").value(chatMessageList.get(0).getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[0].createdAt").value(chatMessageList.get(0).getCreatedAtString()))
                .andExpect(jsonPath("$.data.messages[0].content").value(chatMessageList.get(0).getContent()))
                .andExpect(jsonPath("$.data.messages[0].sender.userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[0].sender.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("$.data.messages[0].sender.profileImg").value(user1.getProfileImg()))
                .andExpect(jsonPath("$.data.messages[1].chatMessageId").value(chatMessageList.get(1).getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[1].createdAt").value(chatMessageList.get(1).getCreatedAtString()))
                .andExpect(jsonPath("$.data.messages[1].content").value(chatMessageList.get(1).getContent()))
                .andExpect(jsonPath("$.data.messages[1].sender.userId").value(user2.getUuid().toString()))
                .andExpect(jsonPath("$.data.messages[1].sender.nickname").value(user2.getNickname()))
                .andExpect(jsonPath("$.data.messages[1].sender.profileImg").value(user2.getProfileImg()))
        ;


        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값"),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값")
                                ),
                                pathParameters(
                                        parameterWithName("chatRoomId").description("조회할 채팅방 식별자")
                                ),
                                requestParameters(
                                        parameterWithName("page").description("조회할 페이지 번호"),
                                        parameterWithName("size").description("조회할 한 페이지 크기")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("page").type("Integer").description("조회된 페이지 번호"),
                                        fieldWithPath("size").type("Integer").description("조회된 한 페이지 크기"),
                                        fieldWithPath("messages[].chatMessageId").type("UUID").description("채팅 메세지 식별자"),
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