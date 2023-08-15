package hatch.hatchserver2023.domain.chat.repository;

import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.domain.UserChatRoom;
import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    List<UserChatRoom> findAllByUser(User user);

    @Query(value = "select * from user_chat_room as r where r.chat_room_id=:chatRoomId and not r.user_id=:userId limit 1", nativeQuery = true)
    UserChatRoom findByChatRoomNotMeOne(@Param("chatRoomId") long chatRoomId, @Param("userId") long userId);

}
