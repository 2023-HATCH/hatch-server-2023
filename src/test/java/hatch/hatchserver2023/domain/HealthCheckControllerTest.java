package hatch.hatchserver2023.domain;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = {HealthCheckController.class})
@MockBean(JpaMetamodelMappingContext.class) //jpa 관련 bean 필요할 때만
@AutoConfigureRestDocs // rest docs 자동 설정
class HealthCheckControllerTest {

    @Autowired // 한글깨짐 현상 있을 수 있음
    private MockMvc mockMvc;

    @Test
    @WithMockUser
        //401 에러 방지 : Spring security 는 모든 요청에서 항상 권한을 확인하므로 미인증 사용자라도 넣어줘야 함
        //Mock 사용자를 생성해서 함께 요청. test/hello 는 아무 권한도 필요하지 않은 api 이므로, Mock 사용자에 대한 설정없이 이 어노테이션을 달아주기만 하면 됨.
    void healthCheck() throws Exception {
        //given
        //when
        //then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
//                .andExpect(content().string("Pocket Pose API Server : Health check ok. Server is running"))
                .andExpect(jsonPath("$.code").value(CommonCode.OK.getCode()))
                .andExpect(jsonPath("$.message").value(CommonCode.OK.getMessage()))
                .andExpect(jsonPath("$.data").value("Pocket Pose API Server : Health check ok. Server is running"))
                .andDo(print()) // 요청, 응답 내용 출력
                .andDo( // rest docs
                        document("health-check",  // 문서 조각 디렉토리 명
                                responseFields( //body 로 오는 필드 전부 넣어줘야 테스트 통과함. 아니면 responseFields() 를 아예 빼야 함
//                                    beneathPath("data"), //추후 추가
                                        fieldWithPath("timeStamp").type(JsonFieldType.STRING).description("응답 시각"),
                                        fieldWithPath("code").type("Integer").description("응답 status code"),
                                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메세지"),
                                        fieldWithPath("data").type(JsonFieldType.STRING).description("응답 데이터").optional()
                                )
                        )
                );
    }
}