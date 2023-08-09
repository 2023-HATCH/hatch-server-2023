package hatch.hatchserver2023.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class ObjectMapperUtil {

    public <T> String toJson(T data) {
        try{
            return new ObjectMapper().writeValueAsString(data);
        }catch (JsonProcessingException e) {
            log.info("ObjectMapperUtil toJson : JsonProcessingException");
            log.info(e.getMessage());
            throw new RuntimeException(e); //TODO
        }
    }

    public <T> T toOriginalType(String json, Class<T> classType) throws JsonProcessingException {
        try{
            return new ObjectMapper().readValue(json, classType);
        } catch (JsonProcessingException e) {
            log.info("ObjectMapperUtil toOriginalType : JsonProcessingException");
            log.info(e.getMessage());
            throw e; //TODO
        }
    }
}
