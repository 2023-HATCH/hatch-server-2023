package hatch.hatchserver2023.domain.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.user.application.AuthService;
import hatch.hatchserver2023.domain.user.application.KakaoService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = {AuthController.class})
@MockBean(JpaMetamodelMappingContext.class)
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RestDocumentationResultHandler docs;

    @MockBean
    private KakaoService kakaoService;

    @MockBean
    private AuthService authService;


    private UUID uuid;
    private Long kakaoAccountNumber;
    private String email;
    private String nickname;
    private String profileImg;

    @BeforeEach
    void setup(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))  // rest docs 설정 주입
                .alwaysDo(MockMvcResultHandlers.print()) // andDo(print()) 코드 포함
                .alwaysDo(docs) // pretty 패턴과 문서 디렉토리 명 정해준것 적용
                .addFilters(new CharacterEncodingFilter("UTF-8", true)) // 한글 깨짐 방지
                .build();

        log.info("set up");
        uuid = UUID.randomUUID();
        kakaoAccountNumber = 99999L;
        email = "test@email.com";
        nickname = "nicknameTest";
        profileImg = "http://testurl";
    }

    @Test
    @WithMockUser
    void login() throws Exception { //TODO : 쿠키 토큰 로직은 service 코드에서 검증
        //given
        UserRequestDto.KakaoLogin requestDto = UserRequestDto.KakaoLogin.builder()
                .kakaoAccessToken("dummyToken")
                .fcmNotificationToken("fcm으로부터받은토큰값")
                .build();
        String requestDtoString = new ObjectMapper().writeValueAsString(requestDto);
        KakaoDto.GetUserInfo kakaoInfoDto = KakaoDto.GetUserInfo.builder()
                .kakaoAccountNumber(kakaoAccountNumber)
                .email(email)
                .nickname(nickname)
                .profileImg(profileImg)
                .build();
        User resultUser = User.builder()
                .uuid(uuid)
                .nickname(nickname)
                .email(email)
                .profileImg(profileImg)
                .build();
//        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        //when
        when(kakaoService.getUserInfo(requestDto.getKakaoAccessToken())).thenReturn(kakaoInfoDto);
        when(authService.signUpAndLogin(any(KakaoDto.GetUserInfo.class), eq(requestDto.getFcmNotificationToken()), any())).thenReturn(resultUser);

        //then
        MockHttpServletRequestBuilder requestPost = post("/api/v1/auth/login")
                .param("type","kakao")
                .content(requestDtoString)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestPost);

        StatusCode code = UserStatusCode.KAKAO_LOGIN_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()))
                .andExpect(jsonPath("$.data.userId").value(resultUser.getUuid().toString()))
                .andExpect(jsonPath("$.data.nickname").value(resultUser.getNickname()))
                .andExpect(jsonPath("$.data.email").value(resultUser.getEmail()))
                .andExpect(jsonPath("$.data.profileImg").value(resultUser.getProfileImg()))
        ;

        //docs
        resultActions
                .andDo(
                    docs.document(
                        requestParameters(
                                parameterWithName("type").description("로그인 유형. 고정값(kakao)")
                        ),
                        requestFields(
                                fieldWithPath("kakaoAccessToken").type(JsonFieldType.STRING).description("카카오로부터 받은 액세스 토큰"), //.attributes(field("constraints", "")
                                fieldWithPath("fcmNotificationToken").type(JsonFieldType.STRING).description("FCM으로부터 받은 토큰") //.attributes(field("constraints", "")
                        ),
                        responseFields(
                                beneathPath("data"),
                                fieldWithPath("userId").type(JsonFieldType.STRING).description("사용자 식별자"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("사용자 닉네임(카카오에서 가져온 데이터)"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일(카카오에서 가져온 데이터)").optional(),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("사용자 프로필 이미지(카카오에서 가져온 데이터)").optional()
                                )
                        )
                )
        ;
    }


    @Test
    @WithCustomAuth(nickname = "nicknameTest", profileImg = "http://testurl", role="ROLE_USER") // @AuthenticationPrincipal 처리
    void kakaoLogout() throws Exception {
        //given
        //when
        //then
        MockHttpServletRequestBuilder requestDelete = delete("/api/v1/auth/logout")
                .param("type","kakao")
                .header("x-access-token", "액세스 토큰 값")
                .header("x-refresh-token", "리프레시 토큰 값")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf().asHeader()); // csrf가 request parameter 로 들어갈 경우 문서화 필수 오류 해결

        ResultActions resultActions = mockMvc.perform(requestDelete);

        StatusCode code = UserStatusCode.KAKAO_LOGOUT_SUCCESS;
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code.getCode()))
                .andExpect(jsonPath("$.message").value(code.getMessage()));

        //docs
        resultActions
                .andDo(
                        docs.document(
                                requestHeaders(
                                        headerWithName("x-access-token").description("액세스 토큰 값. 토큰이 있어야 FCM 토큰 처리까지 깔끔하게 됨. 없어도 에러는 나지 않음").optional(),
                                        headerWithName("x-refresh-token").description("리프레시 토큰 값").optional()
                                ),
                                requestParameters(
                                        parameterWithName("type").description("로그인 유형. 고정값(kakao)")
                                )
                        )
                )
        ;
    }
}