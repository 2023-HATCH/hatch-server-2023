package hatch.hatchserver2023.domain.talk.application;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import hatch.hatchserver2023.domain.talk.repository.TalkRepository;
import hatch.hatchserver2023.domain.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TalkService {
    private final TalkRepository talkRepository;


    public TalkService(TalkRepository talkRepository) {
        this.talkRepository = talkRepository;
    }

    public TalkMessage saveTalkMessage(TalkMessage talkMessage, User sender) {
        log.info("[SERVICE] saveTalkMessage");
        talkMessage.updateUser(sender);
        return talkRepository.save(talkMessage);
    }
}
