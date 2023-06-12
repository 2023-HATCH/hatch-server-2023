package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.global.common.response.code.StatusCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@WithMockUser //401 에러 방지
@AutoConfigureRestDocs // rest docs 자동 설정
public class StatusCodeDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void statusCodeDocumentation() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(get("/status-codes")
                .accept(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk());

        //docs
        result.andDo(document("status-code", // 문서 이름
                // 커스텀 snippet 을 만들어서 반환해주는 함수, 그에 필요한 값들 4개
                codeResponseFields(
                        "code-response",
                        // TODO : statusCodes는 이 api의 응답 Body에서 코드들을 담은 항목이름. StatusCodeView 의 필드명이 그대로 들어감. 생성되는 adoc 파일 명에도 쓰임
                        beneathPath("statusCodes"),
                        attributes(key("title").value("공통 상태 코드")),
                        enumConvertFieldDescriptor(StatusCodeView.getStatusCodeArray())
                )
        ));

        result.andDo(print());
    }

    // 이해 필요
    /**
     * 커스텀 스니펫 CodeResponseFieldsSnippet 생성에 필요한 FieldDescriptor[] 를 반환해주는 메서드
     *
     * @param statusCodes
     * @return
     */
    private FieldDescriptor[] enumConvertFieldDescriptor(StatusCode[] statusCodes) {
        return Arrays.stream(statusCodes)
                .map(enumType -> fieldWithPath(enumType.getCode()).description(enumType.getMessage()))
                .toArray(FieldDescriptor[]::new);
    }

    // 이해 필요
    /**
     * 커스텀 스니펫 CodeResponseFieldsSnippet 를 생성해서 반환해주는 메서드
     *
     * @param type
     * @param subsectionExtractor
     * @param attributes
     * @param descriptors
     * @return
     */
    public static CodeResponseFieldsSnippet codeResponseFields(String type,
                                                               PayloadSubsectionExtractor<?> subsectionExtractor,
                                                               Map<String, Object> attributes,
                                                               FieldDescriptor... descriptors) {
        return new CodeResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes, true);
    }
}
