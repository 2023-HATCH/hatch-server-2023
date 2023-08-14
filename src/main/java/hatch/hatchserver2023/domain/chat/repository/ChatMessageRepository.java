package hatch.hatchserver2023.domain.chat.repository;

import hatch.hatchserver2023.domain.chat.domain.ChatMessage;
import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.talk.domain.TalkMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findAll(Pageable pageable);
    Slice<ChatMessage> findAllByChatRoom(ChatRoom chatRoom, Pageable pageRequest);
}
