package hatch.hatchserver2023.domain.talk.repository;

import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

//import java.awt.print.Pageable;

public interface TalkRepository extends JpaRepository<TalkMessage, Long> {
    Page<TalkMessage> findAll(Pageable pageable);
}
