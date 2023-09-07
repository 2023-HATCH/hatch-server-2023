package hatch.hatchserver2023.domain.chat.application;

import hatch.hatchserver2023.domain.chat.domain.ChatRoom;
import hatch.hatchserver2023.domain.chat.repository.ChatRoomRepository;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.global.common.response.code.ChatStatusCode;
import hatch.hatchserver2023.global.common.response.code.VideoStatusCode;
import hatch.hatchserver2023.global.common.response.exception.ChatException;
import hatch.hatchserver2023.global.common.response.exception.VideoException;
import hatch.hatchserver2023.global.config.redis.RedisDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChatCacheUtil {
    private final String KEY_CACHE_CHAT_ROOM_RECENT_DATA = "chatRoom:recent:"; // +chatRoomId
    private final String HASH_KEY_CHAT_ROOM_RECENT_CONTENT = "content";
    private final String HASH_KEY_CHAT_ROOM_RECENT_SEND_AT = "sendAt";

    private final RedisDao redisDao;

    private final ChatRoomRepository chatRoomRepository;

    public ChatCacheUtil(RedisDao redisDao, ChatRoomRepository chatRoomRepository) {
        this.redisDao = redisDao;
        this.chatRoomRepository = chatRoomRepository;
    }

    // TODO : 락 걸기

    /**
     * 최근 전송 데이터 저장 (redis)
     * @param chatRoom
     * @param content
     * @param sendAt
     */
    public void saveRecentContent(ChatRoom chatRoom, String content, String sendAt) {
        log.info("[REDIS] updateRecentContent");

        Map<String, String> recentDatas = new HashMap<>();
        recentDatas.put(HASH_KEY_CHAT_ROOM_RECENT_CONTENT, content);
        recentDatas.put(HASH_KEY_CHAT_ROOM_RECENT_SEND_AT, sendAt);

        redisDao.setValuesAllHash(toRecentDataKey(chatRoom.getId()), recentDatas);
    }

    /**
     * 최근 전송 데이터 조회 (redis), 채팅방 객체에 반영
     * @param chatRoom
     * @return
     */
    public ChatRoom getRecentUpdatedChatRoom(ChatRoom chatRoom) {
        log.info("[REDIS] getRecentUpdatedChatRoom");
        getUpdatedChatRoom(chatRoom);

        // 레디스에 없거나 일부만 있으면 RDB 에서 가져온 데이터 그대로 반환
        return chatRoom;
    }


    //////////// move to RDB ///////////

    /**
     * 주기적으로 redis의 채팅 최근 데이터를 RDB에 저장하고 redis 데이터 삭제
     */
    // @Scheduled 로 주기적으로 DB에 업데이트
    @Scheduled(fixedRate = 1000 * 60 * 60 * 6) // 6시간마다 실행
//    @Scheduled(fixedRate = 1000 * 10) // 10초 마다 실행(테스트용)
    private void moveChatRecentDataToRDB() {
        log.info("[SCHEDULED] moveChatRecentDataToRDB : start at {}", ZonedDateTime.now());

        Cursor<String> chatRecentKeyCursor = redisDao.getKeys(KEY_CACHE_CHAT_ROOM_RECENT_DATA+"*"); // 최근전송 데이터 key값 목록

        List<ChatRoom> chatRooms = new ArrayList<>();
        List<String> chatRecentKeys = new ArrayList<>();
        makeRecentUpdatedChatRooms(chatRecentKeyCursor, chatRooms, chatRecentKeys);

        log.info("[SCHEDULED] get chatRooms END");
        log.info("[SCHEDULED] chatRooms list size : {}", chatRooms.size());

        chatRoomRepository.saveAll(chatRooms);
        redisDao.deleteValues(chatRecentKeys);
        log.info("[SCHEDULED] moveChatRecentDataToRDB : finish at {}", ZonedDateTime.now());
    }

    private void makeRecentUpdatedChatRooms(Cursor<String> chatRecentKeyCursor, List<ChatRoom> chatRooms, List<String> chatRecentKeys) {
        while (chatRecentKeyCursor.hasNext()) {
            // 이번 키값
            String key = chatRecentKeyCursor.next();
            chatRecentKeys.add(key);
            log.info("[SCHEDULED] key : {}", key);

            // 키값에 해당하는 chatRoom 객체
            ChatRoom chatRoom;
            try{
                chatRoom = getChatRoomFromRDB(key);
            }catch (ChatException e) {
                log.info("{} : {}", key, e.getMessage());
                continue;
            }

            // 값 가져와서 chatRoom 에 반영
            // 최근 전송 메세지, 최근 전송 시각
            getUpdatedChatRoom(chatRoom);

            // 데이터 반영 완료된 chatRoom 모음
            chatRooms.add(chatRoom);
        }
    }


    private ChatRoom getChatRoomFromRDB(String key) {
        // 키값에서 chatRoomId 추출
        long chatRoomId = getIdFromKey(key);

        // video 데이터
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatStatusCode.CHAT_ROOM_NOT_FOUND));
    }

    /**
     * redis 에 데이터가 존재하는 지 확인, 존재하면 해당 값 반영
     * @param chatRoom
     */
    private void getUpdatedChatRoom(ChatRoom chatRoom) {
        // 레디스에 존재하는지 확인
        List<Object> recentDataObject = getRecentDatas(chatRoom.getId());

        Object content = recentDataObject.get(0); // content
        Object sendAt = recentDataObject.get(1); // sendAt

        // 레디스에 있으면 그 데이터로 적용해서 반환
        if (content!=null && sendAt!=null) {
            log.info("[REDIS] {}번 채팅장 최근 전송 캐시데이터 존재함. 반영", chatRoom.getId());
            chatRoom.updateRecentDatas(content.toString(), sendAt.toString());
        }else{
            log.info("[REDIS] {}번 채팅장 최근 전송 캐시데이터 없음", chatRoom.getId());
        }
    }

    /**
     * redis 에서 최근 전송 데이터 전체(2개) 조회
     * @return
     */
    private List<Object> getRecentDatas(long chatRoomId) {
        return redisDao.getValuesAllHash(toRecentDataKey(chatRoomId), getRecentDataHashKeysInOrder());
    }

    /**
     * redis 에서 최근 전송 데이터 조회 시 필요한 순서대로 키값 목록 만들어 반환
     * @return
     */
    private List<Object> getRecentDataHashKeysInOrder() {
        return List.of(HASH_KEY_CHAT_ROOM_RECENT_CONTENT, HASH_KEY_CHAT_ROOM_RECENT_SEND_AT); // 이 키값 순서대로 Redis에서 반환값 List가 정렬됨
    }

    private String toRecentDataKey(Long chatRoomId) {
        return KEY_CACHE_CHAT_ROOM_RECENT_DATA + chatRoomId;
    }

    // TODO : LikeCacheUtil 과 중복
    private long getIdFromKey(String key) {
        String[] keySplit = key.split(":");
        return Long.parseLong(keySplit[keySplit.length-1]);
    }

}
