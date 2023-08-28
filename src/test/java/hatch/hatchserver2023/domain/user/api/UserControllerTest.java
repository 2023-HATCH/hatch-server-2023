package hatch.hatchserver2023.domain.user.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.application.UserUtilService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserModel;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
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

    @MockBean
    LikeService likeService;

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
        boolean isFollowing = true;
        int followingCount = user1.getFollowingCount();
        int followerCount = user1.getFollowerCount();

        UserModel.ProfileInfo profileInfo = UserModel.ProfileInfo.builder()
                                                    .user(user1)
                                                    .isMe(isMe)
                                                    .isFollowing(isFollowing)
                                                    .followingCount(followingCount)
                                                    .followerCount(followerCount)
                                                    .build();

        given(userUtilService.getProfile(eq(user1.getUuid()), any()))
                .willReturn(profileInfo);

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
                                        fieldWithPath("isFollowing").type(JsonFieldType.BOOLEAN).description("로그인한 사용자가 해당 사용자를 팔로잉하는지 여부 (비회원이면 false)"),
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
    @DisplayName("Get User's Uploaded Video List")
    void getUsersVideoList() throws Exception {
        //given
        Video video1 = Video.builder()
                .id(999L)
                .uuid(UUID.randomUUID())
                .title("타이틀 1")
                .tag("#해시 #태그")
                .user(user1)
                .videoUrl("동영상 s3 경로 1")
                .thumbnailUrl("썸네일 이미지 s3 경로 1")
                .likeCount(3)
                .commentCount(11)
                .length(107800)
                .build();

        Video video2 = Video.builder()
                .id(998L)
                .uuid(UUID.randomUUID())
                .title("타이틀 2")
                .tag("#해시 #태그 #2")
                .user(user1)
                .videoUrl("동영상 s3 경로 2")
                .thumbnailUrl("썸네일 이미지 s3 경로 2")
                .likeCount(5)
                .commentCount(2)
                .length(9999)
                .build();

        VideoModel.VideoInfo videoInfo1 = VideoModel.VideoInfo.builder()
                                                    .video(video1)
                                                    .isLiked(false)
                                                    .viewCount(2)
                                                    .commentCount(3)
                                                    .viewCount(4)
                                                    .build();
        VideoModel.VideoInfo videoInfo2 = VideoModel.VideoInfo.builder()
                                                    .video(video2)
                                                    .isLiked(true)
                                                    .viewCount(5)
                                                    .commentCount(6)
                                                    .viewCount(7)
                                                    .build();
        List<VideoModel.VideoInfo> videoList = Arrays.asList(videoInfo1, videoInfo2);
        Slice<VideoModel.VideoInfo> slice = new SliceImpl<>(videoList, PageRequest.of(0, 2), false);

        given(userUtilService.getUsersVideoList(eq(user1.getUuid()), any(), any(Pageable.class)))
                .willReturn(slice);

        //when
        StatusCode code = UserStatusCode.GET_USERS_VIDEO_LIST_SUCCESS_FOR_ANONYMOUS;

        MockHttpServletRequestBuilder requestGet = RestDocumentationRequestBuilders
                .get("/api/v1/users/videos/{userId}", user1.getUuid())
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
                .andExpect(jsonPath("$.data.videoList[0].user.userId").value(video1.getUser().getUuid().toString()))
                .andExpect(jsonPath("$.data.videoList[1].user.userId").value(video2.getUser().getUuid().toString()))
        ;

        resultActions
                .andDo( //rest docs 문서 작성 시작
                        docs.document(
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n\n liked의 차이").optional(),
                                        headerWithName("headerXRefreshToken").description("로그인한 사용자면 같이 보내주시고, 비회원이라면 보내지 않으면 됩니다.\n\n liked의 차이").optional()
                                ),
                                requestParameters(
                                        parameterWithName("page").description("페이지 번호(0부터 시작)"),
                                        parameterWithName("size").description("페이지 크기")
                                ),
                                responseFields(
                                        beneathPath("data.videoList").withSubsectionId("beneath-data-video-list"),
                                        fieldWithPath("uuid").type(JsonFieldType.STRING).description("생성된 동영상 식별자 UUID"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("영상 제목"),
                                        fieldWithPath("tag").type(JsonFieldType.STRING).description("해시태그"),
                                        fieldWithPath("user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("사용자 이메일"),
                                        fieldWithPath("user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임"),
                                        fieldWithPath("user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 경로"),
                                        fieldWithPath("videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로"),
                                        fieldWithPath("thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수"),
                                        fieldWithPath("commentCount").type(JsonFieldType.NUMBER).description("댓글 개수"),
                                        fieldWithPath("viewCount").type(JsonFieldType.NUMBER).description("조회수"),
                                        fieldWithPath("length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이"),
                                        fieldWithPath("createdAt").type("DateTime").description("생성 시각"),
                                        fieldWithPath("liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부")
                                ),
                                responseFields(
                                        beneathPath("data"),
                                        fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                        fieldWithPath("videoList.[].uuid").type(JsonFieldType.STRING).description("동영상 식별자 UUID").ignored(),
                                        fieldWithPath("videoList.[].title").type(JsonFieldType.STRING).description("영상 제목").ignored(),
                                        fieldWithPath("videoList.[].tag").type(JsonFieldType.STRING).description("해시태그").ignored(),
                                        fieldWithPath("videoList.[].user.userId").type(JsonFieldType.STRING).description("작성 사용자 식별자 uuid").ignored(),
                                        fieldWithPath("videoList.[].user.email").type(JsonFieldType.STRING).description("사용자 이메일").ignored(),
                                        fieldWithPath("videoList.[].user.nickname").type(JsonFieldType.STRING).description("사용자 닉네임").ignored(),
                                        fieldWithPath("videoList.[].user.profileImg").type(JsonFieldType.STRING).description("사용자 프로필 사진 S3 경로").ignored(),
                                        fieldWithPath("videoList.[].videoUrl").type(JsonFieldType.STRING).description("동영상 S3 경로").ignored(),
                                        fieldWithPath("videoList.[].thumbnailUrl").type(JsonFieldType.STRING).description("썸네일 이미지 S3 경로").ignored(),
                                        fieldWithPath("videoList.[].likeCount").type(JsonFieldType.NUMBER).description("좋아요 개수").ignored(),
                                        fieldWithPath("videoList.[].commentCount").type(JsonFieldType.NUMBER).description("댓글 개수").ignored(),
                                        fieldWithPath("videoList.[].viewCount").type(JsonFieldType.NUMBER).description("조회수").ignored(),
                                        fieldWithPath("videoList.[].length").type(JsonFieldType.NUMBER).description("milliseconds 단위 동영상 길이").ignored(),
                                        fieldWithPath("videoList.[].createdAt").type("DateTime").description("생성 시각").ignored(),
                                        fieldWithPath("videoList.[].liked").type(JsonFieldType.BOOLEAN).description("좋아요 눌렀는지 여부").ignored()
                                )
                        )
                );
    }


    @Test
    @DisplayName("Update User Profile")
    void updateProfile() throws Exception {
        //given
//        given(userUtilService.updateProfile(user1, "자기소개 수정", "인스타 계정 수정", "트위터 계정 수정"));
        ObjectMapper objectMapper = new ObjectMapper();

        UserRequestDto.UpdateProfile request = UserRequestDto.UpdateProfile.builder()
                .introduce("자기소개 수정")
                .instagramId("인스타 계정 수정")
                .twitterId("트위터 계정 수정")
                .build();

        //when
        StatusCode code = UserStatusCode.UPDATE_MY_PROFILE_SUCCESS;

        MockHttpServletRequestBuilder requestPatch = RestDocumentationRequestBuilders
                .patch("/api/v1/users/me")
                .content(objectMapper.writeValueAsString(request))
                .header("headerXAccessToken", "headerXAccessToken")
                .header("headerXRefreshToken", "headerXRefreshToken")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .with(csrf());

        //then
        ResultActions resultActions = mockMvc.perform(requestPatch);

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true))
        ;

        resultActions
                .andDo( //rest docs 문서 작성 시작
                        docs.document(
                                requestParameters(
                                        parameterWithName("_csrf").description("테스트할 때 넣은 csrf 이므로 무시").ignored()
                                ),
                                requestHeaders(
                                        headerWithName("headerXAccessToken").description("프로필을 수정하려는 로그인 사용자 (필수)"),
                                        headerWithName("headerXRefreshToken").description("프로필을 수정하려는 로그인 사용자 (필수)")
                                ),
                                requestFields(
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개 수정").attributes(key("constraints").value("null 가능")),
                                        fieldWithPath("instagramId").type(JsonFieldType.STRING).description("인스타 계정 수정").attributes(key("constraints").value("null 가능")),
                                        fieldWithPath("twitterId").type(JsonFieldType.STRING).description("트위터 계정 수정").attributes(key("constraints").value("null 가능"))
                                ),
                                responseFields( // response 필드 정보 입력
                                        beneathPath("data"),
                                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("수정 성공 여부 (default: true)")
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
                                        fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 경로"),
                                        fieldWithPath("introduce").type(JsonFieldType.STRING).description("자기소개")
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
        UserModel.FollowInfo follow1 =  UserModel.FollowInfo.toModel(user1, false);
        UserModel.FollowInfo follow3 =  UserModel.FollowInfo.toModel(user3, true);

        List<UserModel.FollowInfo> followerList = Arrays.asList(follow1, follow3);
        List<UserModel.FollowInfo> followingList = Arrays.asList(follow3);

        given(userUtilService.findOneByUuid(user2.getUuid()))
                .willReturn(user2);
        given(userUtilService.getFollowerList(user2, null))
                .willReturn(followerList);
        given(userUtilService.getFollowingList(user2, null))
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
