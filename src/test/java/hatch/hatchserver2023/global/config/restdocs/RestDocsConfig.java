package hatch.hatchserver2023.global.config.restdocs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.restdocs.snippet.Attributes.Attribute;

@TestConfiguration
public class RestDocsConfig {
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
