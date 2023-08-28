package hatch.hatchserver2023.domain.video.application;

import hatch.hatchserver2023.domain.like.application.LikeService;
import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.VideoCacheUtil;
import hatch.hatchserver2023.domain.video.domain.Hashtag;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.domain.VideoHashtag;
import hatch.hatchserver2023.domain.video.dto.VideoModel;
import hatch.hatchserver2023.domain.video.repository.HashtagRepository;
import hatch.hatchserver2023.domain.video.repository.VideoHashtagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final VideoHashtagRepository videoHashtagRepository;
    private final LikeService likeService;
    private final VideoCacheUtil videoCacheUtil;

    public HashtagService(HashtagRepository hashtagRepository, VideoHashtagRepository videoHashtagRepository, LikeService likeService, VideoCacheUtil videoCacheUtil){
        this.hashtagRepository = hashtagRepository;
        this.videoHashtagRepository = videoHashtagRepository;
        this.likeService = likeService;
        this.videoCacheUtil = videoCacheUtil;
    }


    /**
     * 해시태그 목록 조회
     * @return tagList
     */
    public List<String> getHashtagList() {
        List<Hashtag> hashtagList = hashtagRepository.findAll();
        List<String> tagList = new ArrayList<>();

        for(Hashtag hashtag : hashtagList) {
            tagList.add(hashtag.getTitle());
        }

        return tagList;
    }


    /**
     * 해시태그 검색
     *
     * @param tag
     * @param pageable
     * @return videoList
     */
    public Slice<VideoModel.VideoInfo> searchHashtag(String tag, User loginUser, Pageable pageable){

        List<Video> videoList = new ArrayList<>();

        Optional<Hashtag> hashtag =  hashtagRepository.findByTitle(tag);

        boolean hasNext = false;

        if(hashtag.isPresent()){
            // 검색한 해시태그가 존재한다면, videoList 제작
            Slice<VideoHashtag> mapSlice = videoHashtagRepository.findAllByHashtag(hashtag.get(), pageable);
            hasNext = mapSlice.hasNext();
            List<VideoHashtag> mapList = mapSlice.getContent();

            for (VideoHashtag map : mapList){
                videoList.add(map.getVideo());
            }
        }  // 검색한 해시태그가 없다면 videoList는 빈 배열

        List<VideoModel.VideoInfo> videoInfoList;

        // 비회원: isLiked가 모두 false
        if(loginUser == null) {
            videoInfoList = videoList.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(false)
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }
        //회원: isLiked 여부 확인
        else {
            videoInfoList = videoList.stream()
                    .map(one -> VideoModel.VideoInfo.builder()
                            .video(one)
                            .isLiked(likeService.isAlreadyLiked(one, loginUser))
                            .viewCount(videoCacheUtil.getViewCount(one))
                            .likeCount(videoCacheUtil.getLikeCount(one))
                            .commentCount(videoCacheUtil.getCommentCount(one))
                            .build())
                    .collect(Collectors.toList());
        }

        //paginaton 적용
        //no-offset
        Slice<VideoModel.VideoInfo> videoSlice = new SliceImpl<>(videoInfoList, pageable, hasNext);

        return videoSlice;
    }


    /**
     * 해시태그 파싱 후 저장
     * - 동영상 업로드 할 때 사용
     *
     * @param tag
     * @param video
     * @return null
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveHashtag(String tag, Video video){
        // tag를 파싱
        String[] parsedTags = tag.split("#");

        for(String parsedTag : parsedTags){

            if(parsedTag.isBlank()){
                continue;
            }

            String tagTitle = parsedTag.strip();

            //기존에 없는 해시태그면, 새로운 Hashtag 만들고 videoList도 새로 만들어 DB에 저장
            //기존에 있는 해시태그면, 기존 videoList에 새로운 video 추가
            Hashtag hashtag = hashtagRepository.findByTitle(tagTitle)
                    .orElse(Hashtag.createHashtag(tagTitle));
            hashtagRepository.save(hashtag);

            //매핑 테이블에도 추가
            VideoHashtag map = VideoHashtag.builder()
                                .video(video)
                                .hashtag(hashtag)
                                .build();

            videoHashtagRepository.save(map);
        }

    }

    /**
     * 영상 삭제 시 연관 해시태그들도 같이 삭제
     *
     * @param video
     */
    @Transactional
    public void deleteHashtagByVideo(Video video){

        List<VideoHashtag> maps = videoHashtagRepository.findAllByVideo(video);
        List<Hashtag> hashtagList = maps.stream()
                .map(VideoHashtag::getHashtag)
                .collect(Collectors.toList());

        hashtagList.forEach(hashtagRepository::delete);
    }

    //해시태그 삭제 - 테스트용
    @Transactional
    public void deleteByTitle(String title){
        Hashtag hashtag = hashtagRepository.findByTitle(title).get();

        //해시태그 db에서 삭제
        hashtagRepository.delete(hashtag);
    }

}
