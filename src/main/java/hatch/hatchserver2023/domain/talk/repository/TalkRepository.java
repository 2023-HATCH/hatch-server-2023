package hatch.hatchserver2023.domain.talk.repository;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkRepository extends JpaRepository<TalkMessage, Long> {
}
