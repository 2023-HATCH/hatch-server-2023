package hatch.hatchserver2023.domain.chat.repository;

import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
}
