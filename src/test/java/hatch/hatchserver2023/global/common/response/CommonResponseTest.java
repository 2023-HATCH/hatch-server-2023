package hatch.hatchserver2023.global.common.response;

import hatch.hatchserver2023.global.common.response.code.CommonCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CommonResponseTest {

    @Test
    void toResponse() {
        //given
        CommonCode code = CommonCode.OK;

        //when
        //then
        CommonResponse response = CommonResponse.toResponse(code);
        assertThat(response.getCode())
                .isEqualTo("COMMON-200");
        assertThat(response.getMessage())
                .isEqualTo("[공통] 정상 처리");
    }


    @Test
    void toErrorResponse() {
        //given
        CommonCode code = CommonCode.BAD_REQUEST;

        //when
        //then
        CommonResponse response = CommonResponse.toResponse(code);
        assertThat(response.getCode())
                .isEqualTo("COMMON-400");
        assertThat(response.getMessage())
                .isEqualTo("[공통] 잘못된 요청");
    }

}