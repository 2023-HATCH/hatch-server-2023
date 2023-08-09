package hatch.hatchserver2023.domain.user.application;

import hatch.hatchserver2023.domain.user.domain.Follow;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.FollowRepository;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class UserUtilService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserUtilService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }


    public List<User> getUsersById(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }



    /**
     * uuid로 한 명의 사용자 찾기
     *
     * @param userId
     * @return user
     */
    public User findOneByUuid(UUID userId) {
        User user = userRepository.findByUuid(userId)
                .orElseThrow(() -> new UserException(UserStatusCode.USER_NOT_FOUND));

        return user;
    }


    /**
     * 팔로우 추가
     *
     * @param fromUser
     * @param toUser
     */
    public void addFollow(User fromUser, User toUser) {
        if (fromUser.equals(toUser)) {
            throw new UserException(UserStatusCode.CANT_FOLLOW_YOURSELF);
        }

        Follow follow = Follow.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();

        followRepository.save(follow);
    }


    /**
     * 팔로우 삭제
     *
     * @param fromUser
     * @param toUser
     */
    public void deleteFollow(User fromUser, User toUser) {

        Follow follow = followRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new UserException(UserStatusCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }



    // 팔로워 목록 조회
    public List<User> getFollowerList(User user){
        List<Follow> followList = followRepository.findAllByToUser(user);
        List<User> followerList = transferFollowListToUserList(followList, "fromUser");
        return followerList;
    }

    // 팔로잉 목록 조회
    public List<User> getFollowingList(User user){
        List<Follow> followList = followRepository.findAllByFromUser(user);
        List<User> followingList = transferFollowListToUserList(followList, "toUser");
        return followingList;
    }

    private List<User> transferFollowListToUserList(List<Follow> followList, String option) {
        List<User> userList = new ArrayList<>();

        if(Objects.equals(option, "fromUser")){
            for(Follow follow : followList) {
                userList.add(follow.getFromUser());
            }
        } else {    //toUser
            for(Follow follow : followList) {
                userList.add(follow.getToUser());
            }
        }
        return userList;
    }


    //팔로우 여부
    public Boolean isFollowing(User fromUser, User toUser) {
        return followRepository.findByFromUserAndToUser(fromUser, toUser).isPresent();
    }

}
