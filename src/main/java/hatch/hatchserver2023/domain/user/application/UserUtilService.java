package hatch.hatchserver2023.domain.user.application;

import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.domain.Follow;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.dto.UserModel;
import hatch.hatchserver2023.domain.user.repository.FollowRepository;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.repository.VideoRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserUtilService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final VideoRepository videoRepository;
    private final LikeService likeService;

    public UserUtilService(UserRepository userRepository, FollowRepository followRepository, VideoRepository videoRepository, LikeService likeService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.videoRepository = videoRepository;
        this.likeService = likeService;
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

    //팔로워 수 세기
    public int countFollower(User toUser) {
        return followRepository.countByToUser(toUser);
    }

    //팔로잉 수 세기
    public int countFollowing(User fromUser) {
        return followRepository.countByFromUser(fromUser);
    }


    /**
     * 업로드한 영상 목록 조회
     *
     * @param userId
     * @param pageable
     * @return
     */
    public Slice<VideoModel.VideoInfo> getUsersVideoList(UUID userId, User loginUser, Pageable pageable) {
        User user = findOneByUuid(userId);

        Slice<Video> videoSlice = videoRepository.findAllByUserId(user, pageable);

        List<VideoModel.VideoInfo> videoInfoList;

        //비회원: liked는 모두 false
        if (loginUser == null) {
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.toModel(one, false))
                    .collect(Collectors.toList());
        }
        //회원: 영상 좋아요 여부 liked 지정
        else{
            videoInfoList = videoSlice.stream()
                    .map(one -> VideoModel.VideoInfo.toModel(one, likeService.isAlreadyLiked(one, loginUser)))
                    .collect(Collectors.toList());
        }

        Slice<VideoModel.VideoInfo> videoInfoSlice = new SliceImpl<>(videoInfoList, pageable, videoSlice.hasNext());
        return videoInfoSlice;
    }


    //프로필 수정
    public void updateProfile(User user, String introduce, String instagramId, String twitterId) {

        user.updateProfile(introduce, instagramId, twitterId);
        userRepository.save(user);
    }


    /**
     * 계정 검색
     * - 닉네임 1순위, 이메일 2순위로 검색
     *
     * @param key
     * @param pageable
     * @return
     */
    //TODO: pageable 적용 안해놓음. 명세서에도 안적어놓음
    public List<User> searchUsers(String key, Pageable pageable) {

        //닉네임으로 검색
        List<User> searchedByNickname = userRepository.findAllByNicknameContainingIgnoreCase(key, pageable);

        //이메일로 검색
        List<User> searchedByEmail = userRepository.findAllByEmailContainingIgnoreCase(key, pageable);

        //리스트 붙이고 중복 제거 (중복이면 뒤에거 제거)
        List<User> searchList = Stream
                                .concat(searchedByNickname.stream(), searchedByEmail.stream())
                                .distinct()
                                .collect(Collectors.toList());

        return searchList;
    }


    /**
     * 팔로우 추가
     *
     * @param fromUser
     * @param toUser
     */
    public void addFollow(User fromUser, User toUser) {
        if (fromUser.getUuid().equals(toUser.getUuid())) {
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
    public List<UserModel.FollowInfo> getFollowerList(User user, User loginUser){
        List<Follow> followList = followRepository.findAllByToUser(user);
        List<UserModel.FollowInfo> followerList = transferFollowListToFollowInfoList(followList, loginUser, "fromUser");
        return followerList;
    }

    // 팔로잉 목록 조회
    public List<UserModel.FollowInfo> getFollowingList(User user, User loginUser){
        List<Follow> followList = followRepository.findAllByFromUser(user);
        List<UserModel.FollowInfo> followingList = transferFollowListToFollowInfoList(followList, loginUser, "toUser");
        return followingList;
    }

    private List<UserModel.FollowInfo> transferFollowListToFollowInfoList(List<Follow> followList, User loginUser, String option) {
        List<UserModel.FollowInfo> followInfoList = new ArrayList<>();

        if(Objects.equals(option, "fromUser")){
            for(Follow follow : followList) {
                User user = follow.getFromUser();
                followInfoList.add(UserModel.FollowInfo.toModel(user, isFollowing(loginUser, user)));
            }
        } else {    //toUser
            for(Follow follow : followList) {
                User user = follow.getToUser();
                followInfoList.add(UserModel.FollowInfo.toModel(user, isFollowing(loginUser, user)));
            }
        }
        return followInfoList;
    }


    //팔로우 여부
    public Boolean isFollowing(User fromUser, User toUser) {
//        if(fromUser == null) {
//            return false;
//        }
        return followRepository.findByFromUserAndToUser(fromUser, toUser).isPresent();
    }

}
