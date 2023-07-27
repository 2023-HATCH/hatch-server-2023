package hatch.hatchserver2023.domain.talk.application;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.repository.TalkRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

//import java.awt.print.Pageable;
import java.util.List;

@Slf4j
@Service
public class TalkService {
    private final TalkRepository talkRepository;

    public TalkService(TalkRepository talkRepository) {
        this.talkRepository = talkRepository;
    }

    /**
     * 스테이지 라이브톡 전송 메세지 DB 저장 로직
     * @param talkMessage : 저장할 메세지
     * @param sender : 전송한 사용자
     * @return
     */
    public TalkMessage saveTalkMessage(TalkMessage talkMessage, User sender) {
        log.info("[SERVICE] saveTalkMessage");
        talkMessage.updateUser(sender);
        return talkRepository.save(talkMessage);
    }


    /**
     * 스테이지 라이브톡 메세지 목록 조회 로직
     * @param page
     * @param size
     * @return
     */
    public Slice<TalkMessage> getTalkMessages(int page, int size) {
        log.info("[SERVICE] getTalkMessages");
        Pageable pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending()); //service 로?
        return talkRepository.findAll(pageRequest);
    }
}
