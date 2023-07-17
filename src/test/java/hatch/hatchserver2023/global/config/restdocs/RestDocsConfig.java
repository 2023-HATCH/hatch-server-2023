package hatch.hatchserver2023.global.config.restdocs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.snippet.Attributes.Attribute;

@TestConfiguration
public class RestDocsConfig {

    //한글 깨짐 해결 & json 예쁘게 출력
    @Bean
    public RestDocumentationResultHandler handler() {
        return MockMvcRestDocumentation.document(
                "{method-name}",
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
        );
    }

    public static final Attribute parameter (
            final String key,
            final String value){
        return new Attribute(key,value);
    }

    public static final Attribute field(
            final String key,
            final String value){
        return new Attribute(key,value);
    }
}
