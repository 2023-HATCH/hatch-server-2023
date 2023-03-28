package hatch.hatchserver2023.global.common.response.code;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommonCodeTest {

    @Test
    void getStatus() {
        //given
        //when
        //then
        assertThat(CommonCode.OK.getStatus())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void getCode() {
        //given
        //when
        //then
        assertThat(CommonCode.OK.getCode())
                .isEqualTo("C200");
    }

    @Test
    void getMessage() {
        //given
        //when
        //then
        assertThat(CommonCode.OK.getMessage())
                .isEqualTo("[공통] 정상 처리");
    }
}