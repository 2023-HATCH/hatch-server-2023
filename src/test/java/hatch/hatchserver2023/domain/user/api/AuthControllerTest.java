package hatch.hatchserver2023.domain.user.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hatch.hatchserver2023.domain.user.application.AuthService;
import hatch.hatchserver2023.domain.user.application.KakaoService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.KakaoDto;
import hatch.hatchserver2023.domain.user.dto.UserRequestDto;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static hatch.hatchserver2023.global.config.restdocs.RestDocsConfig.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(controllers = {AuthController.class})
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void setup() {
        log.info("set up");
        uuid = UUID.randomUUID();
        kakaoAccountNumber = 99999L;
        email = "test@email.com";
        nickname = "nicknameTest";
        profileImg = "http://testurl";
    }

    @Test
    @WithMockUser
    void kakaoSignUpAndLogin() throws Exception { //TODO : 쿠키 토큰 로직은 service 코드에서 검증
        //given
        UserRequestDto.KakaoLogin requestDto = UserRequestDto.KakaoLogin.builder()
                .kakaoAccessToken("dummyToken")
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
                .build();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        //when
        when(kakaoService.getUserInfo(requestDto.getKakaoAccessToken())).thenReturn(kakaoInfoDto);
        when(authService.signUpAndLogin(any(KakaoDto.GetUserInfo.class), any())).thenReturn(resultUser);

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
                .andExpect(jsonPath("$.data.uuid").value(resultUser.getUuid().toString()))
                .andExpect(jsonPath("$.data.nickname").value(resultUser.getNickname()))
                .andExpect(jsonPath("$.data.email").value(resultUser.getEmail()))
        ;
        resultActions
                .andDo(document("login",
                        requestParameters(
                                parameterWithName("type").description("로그인 유형. 고정값(kakao)")
                        ),
                        requestFields(
                                fieldWithPath("kakaoAccessToken").type(JsonFieldType.STRING).description("카카오로부터 받은 액세스 토큰") //.attributes(field("constraints", "")
                        ),
                        responseFields(
                                beneathPath("data"),
                                fieldWithPath("uuid").type(JsonFieldType.STRING).description("사용자 식별자"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("사용자 닉네임(카카오에서 가져온 데이터)"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("사용자 이메일(카카오에서 가져온 데이터)").optional()
                                )
                        )
                )
                .andDo(print())
        ;
    }
}