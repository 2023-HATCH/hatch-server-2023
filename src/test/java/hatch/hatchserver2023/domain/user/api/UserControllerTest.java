package hatch.hatchserver2023.domain.user.api;


import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
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
import org.springframework.data.domain.Pageable;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class) // Controller 단위 테스트
@MockBean(JpaMetamodelMappingContext.class) // jpaAuditingHandler 에러 해결
@WithMockUser //401 에러 해결
@AutoConfigureRestDocs // rest docs 자동 설정
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@DisplayName("User Controller Unit Test")
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    UserUtilService userUtilService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();

        user1 = User.builder()
                .id(991L)
                .uuid(UUID.randomUUID())
                .email("user1@gmail.com")
                .nickname("user1")
                .instagramAccount("인스타 계정1")
                .twitterAccount("트위터 계정1")
                .kakaoAccountNumber(1L)
                .introduce("user1 입니다")
                .profileImg("프로필 이미지 경로")
                .build();

        user2 = User.builder()
                .id(992L)
                .uuid(UUID.randomUUID())
                .email("user2@gmail.com")
                .nickname("user2")
                .instagramAccount("인스타 계정2")
                .twitterAccount("트위터 계정2")
                .kakaoAccountNumber(2L)
                .introduce("user2 입니다 :)")
                .profileImg("프로필 이미지 경로 2")
                .build();

        user3 = User.builder()
                .id(993L)
                .uuid(UUID.randomUUID())
                .email("user3@gmail.com")
                .nickname("user3")
                .instagramAccount("인스타 계정3")
                .twitterAccount("트위터 계정3")
                .kakaoAccountNumber(3L)
                .introduce("user3 입니다 XD")
                .profileImg("프로필 이미지 경로 3")
                .build();
    }


    @Test
    @DisplayName("Get Profile")
    void getProfile() throws Exception {
        //given
        boolean isMe = false;
        given(userUtilService.findOneByUuid(user1.getUuid()))
                .willReturn(user1);
        given(userUtilService.countFollower(user1))
                .willReturn(2);
        given(userUtilService.countFollowing(user1))
                .willReturn(1);

        //when
        StatusCode code = UserStatusCode.GET_PROFILE_SUCCESS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/users/profile/{userId}", user1.getUuid())
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken");

        //then
        ResultActions resultActions = mockMvc.perform(requestGet);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.nickname").value(user1.getNickname()))
                .andExpect(jsonPath("$.data.introduce").value(user1.getIntroduce()))
                .andExpect(jsonPath("$.data.twitterId").value(user1.getTwitterAccount()))
                .andExpect(jsonPath("$.data.isMe").value(isMe))
        ;


        resultActions
                .andDo( //rest docs 문서 작성 시작
                        docs.document(
                                pathParameters(
                                        parameterWithName("userId").description("사용자 UUID")
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다. \n\n로그인한 사용자가 자신의 프로필을 조회하는지 여부 isMe를 판단하기 위해 받습니다.").optional(),
                                        headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.").optional()
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자 UUID"),
                                        fieldWithPath("isMe").type(JsonFieldType.BOOLEAN).description("로그인한 사용자가 자신의 프로필을 확인하는지 여부"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로"),
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("instagramId").type(JsonFieldType.STRING).description("인스타그램 계정"),
                                        fieldWithPath("twitterId").type(JsonFieldType.STRING).description("트위터 계정"),
                                        fieldWithPath("followingCount").type(JsonFieldType.NUMBER).description("팔로잉 수"),
                                        fieldWithPath("followerCount").type(JsonFieldType.NUMBER).description("팔로워 수"),
                                        fieldWithPath("createdAt").type("DateTime").description("생성 시각"),
                                        fieldWithPath("modifiedAt").type("DateTime").description("수정 시각")
                                )
                        )
                )
        ;
    }

    @Test
    @DisplayName("Search Users")
    void searchUser() throws Exception {
        //given
        String key = "user";
        List<User> userList = Arrays.asList(user1, user2, user3);

        given(userUtilService.searchUsers(eq(key), any(Pageable.class)))
                .willReturn(userList);

        //when
        StatusCode code = UserStatusCode.SEARCH_USERS_SUCCESS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/users/search")
                .param("key", key);

        //then
        ResultActions resultActions = mockMvc.perform(requestGet);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.userList[0].userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.userList[1].userId").value(user2.getUuid().toString()))
                .andExpect(jsonPath("$.data.userList[2].userId").value(user3.getUuid().toString()))
                .andExpect(jsonPath("$.data.userList[0].nickname").value(user1.getNickname()))
                .andExpect(jsonPath("$.data.userList[1].nickname").value(user2.getNickname()))
                .andExpect(jsonPath("$.data.userList[2].nickname").value(user3.getNickname()))
        ;

        resultActions
                .andDo( //rest docs 문서 작성 시작
                        docs.document(
                                requestParameters(
                                        parameterWithName("key").description("검색어 (대소문자 구별 X, 한글 가능)")
                                ),
                                responseFields(
                                        beneathPath("data.userList").withSubsectionId("beneath-data-user-list"),
                                        fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자 UUID"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로")
                                )
                        )
                );


    }


    @Test
    @DisplayName("Add Follow")
    void addFollow() throws Exception {
        // user1 -> user2

        //given
        given(userUtilService.findOneByUuid(user2.getUuid()))
                .willReturn(user2);
//        given(userUtilService.addFollow(user1, user2));

        //when
        StatusCode code = UserStatusCode.ADD_FOLLOW_SUCCESS;

        MockHttpServletRequestBuilder requestPost = RestDocumentationRequestBuilders
                .post("/api/v1/users/follow/{userId}", user2.getUuid())
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
                        docs.document(
                                pathParameters(
                                        parameterWithName("userId").description("팔로우 하고자 하는 사용자 UUID (ToUser)")
                                ),
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("팔로우 신청하는 로그인 사용자 (FromUser)"),
                                        headerWithName("headerXRefreshToken").description("로그인 사용자 (FromUser)")
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("팔로우 추가 성공 여부 (default: true)")
                                )
                        )
                )
        ;

    }


    @Test
    @DisplayName("Delete Follow")
    void deleteFollow() throws Exception {
        // user3 -> user2 delete

        //given
        given(userUtilService.findOneByUuid(user2.getUuid()))
                .willReturn(user2);

//        given(userUtilService.deleteFollow(user3, user2));

        //when
        StatusCode code = UserStatusCode.DELETE_FOLLOW_SUCCESS;

        MockHttpServletRequestBuilder requestDelete = RestDocumentationRequestBuilders
                .delete("/api/v1/users/follow/{userId}", user2.getUuid())
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
                                        parameterWithName("userId").description("팔로우 지우고자 하는 사용자 UUID (ToUser)")
                                ),
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("로그인한 사용자 (FromUser)"),
                                        headerWithName("headerXRefreshToken").description("로그인한 사용자 (FromUser)")
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("팔로우 삭제 성공 여부 (default: true)")
                                )
                        )
                )
        ;
    }

    @Test
    @DisplayName("Get Follow List")
    void getFollowList() throws Exception {
        //given
        List<User> followerList = Arrays.asList(user1, user3);
        List<User> followingList = Arrays.asList(user3);

        given(userUtilService.findOneByUuid(user2.getUuid()))
                .willReturn(user2);
        given(userUtilService.getFollowerList(user2))
                .willReturn(followerList);
        given(userUtilService.getFollowingList(user2))
                .willReturn(followingList);

        //when
        StatusCode code = UserStatusCode.GET_FOLLOW_LIST_SUCCESS_FOR_ANONYMOUS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/users/follow/{userId}", user2.getUuid())
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken");

        //then
        ResultActions resultActions = mockMvc.perform(requestGet);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.followerList[0].userId").value(user1.getUuid().toString()))
                .andExpect(jsonPath("$.data.followerList[1].userId").value(user3.getUuid().toString()))
                .andExpect(jsonPath("$.data.followingList[0].userId").value(user3.getUuid().toString()))
        ;

        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n비회원: isFollowing이 false, 회원: isFollowing 여부").optional(),
                                        headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n비회원: isFollowing이 false, 회원: isFollowing 여부").optional()
                                ),
                                pathParameters(
                                        parameterWithName("userId").description("팔로워/팔로잉을 알고자 하는 사용자 UUID")
                                ),
                                responseFields(
                                        beneathPath("data.followingList").withSubsectionId("beneath-data-following-list"),
                                        fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자 UUID"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로"),
                                        fieldWithPath("isFollowing").type(JsonFieldType.BOOLEAN).description("로그인한 사용자가 팔로우를 하는지 여부")
                                ),
                                responseFields(
                                        beneathPath("data.followerList").withSubsectionId("beneath-data-follower-list"),
                                        fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자 UUID"),
                                        fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로"),
                                        fieldWithPath("isFollowing").type(JsonFieldType.BOOLEAN).description("로그인한 사용자가 팔로우를 하는지 여부")
                                )
                        )
                )
        ;

    }


}
