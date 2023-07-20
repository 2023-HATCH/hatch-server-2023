package hatch.hatchserver2023.domain.talk.api;

import hatch.hatchserver2023.domain.talk.application.TalkService;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.dto.TalkResponseDto;
import hatch.hatchserver2023.global.common.response.CommonResponse;
import hatch.hatchserver2023.global.common.response.code.TalkStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/talks")
public class TalkController {

    private final TalkService talkService;

    public TalkController(TalkService talkService) {
        this.talkService = talkService;
    }

    /**
     * 스테이지 라이브톡 메세지 조회 api (최근순)
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    @GetMapping("/messages")
    public ResponseEntity<CommonResponse> getTalkMessages(@RequestParam @NotNull @Min(0) Integer page,
                                                             @RequestParam @NotNull Integer size) {
        Slice<TalkMessage> talkMessages = talkService.getTalkMessages(page, size);
        return ResponseEntity.ok().body(CommonResponse.toResponse(
                TalkStatusCode.GET_TALK_MESSAGES_SUCCESS, TalkResponseDto.GetMessagesContainer.toDto(talkMessages)));
    }
}
