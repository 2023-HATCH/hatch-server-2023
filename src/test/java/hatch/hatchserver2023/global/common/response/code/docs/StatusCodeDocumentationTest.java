package hatch.hatchserver2023.global.common.response.code.docs;

import hatch.hatchserver2023.domain.HealthCheckController;
import hatch.hatchserver2023.global.common.response.code.StatusCode;
import hatch.hatchserver2023.global.common.response.code.StatusCodeDoc;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {StatusCodeController.class})
@WithMockUser //401 에러 방지
@AutoConfigureRestDocs // rest docs 자동 설정
public class StatusCodeDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    // TODO : 다른 스니펫 파일들 안생기게.. 안되네
//    @Before
//    public void setUp() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
//                .apply(documentationConfiguration(this.restDocumentation).snippets().withDefaults(responseBody()))
//                .build();
//    }

    @Test
    public void statusCodeDoc() throws Exception {
        for(StatusCodeDoc doc:StatusCodeDoc.values()){ // StatusCodeDoc 내용을 가져와서 반복

            //given
            //when
            ResultActions result = mockMvc.perform(get(doc.getFullUrl())
                    .accept(MediaType.APPLICATION_JSON));

            //then
            result.andExpect(status().isOk());

            //docs
            result.andDo(document("status-code", // 문서 이름
                    // 커스텀 snippet 을 만들어서 반환해주는 함수, 그에 필요한 값들 4개
                    codeResponseFields(
                            "code-response", //스니펫 이름, 커스텀 템플릿 이름 인식과 연결됨
                            // statusCodes는 이 api의 응답 Body에서 코드들을 담은 항목이름. StatusCodeView 의 필드명이 그대로 들어감. 생성되는 adoc 파일 명에도 쓰임
                            beneathPath("statusCodes").withSubsectionId(doc.getInitial()),
                            attributes(key("title").value(doc.getInitial()), key("subtitle").value(doc.getSubtitle())),
                            enumConvertFieldDescriptor(doc.getStatusCodes())
                    )
            ));

            result.andDo(print());
        }
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
