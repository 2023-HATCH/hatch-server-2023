package hatch.hatchserver2023.domain.video.application;

import hatch.hatchserver2023.domain.video.domain.Hashtag;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.domain.VideoHashtag;
import hatch.hatchserver2023.domain.video.repository.HashtagRepository;
import hatch.hatchserver2023.domain.video.repository.VideoHashtagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final VideoHashtagRepository videoHashtagRepository;

    public HashtagService(HashtagRepository hashtagRepository, VideoHashtagRepository videoHashtagRepository){
        this.hashtagRepository = hashtagRepository;
        this.videoHashtagRepository = videoHashtagRepository;
    }

    //해시태그 목록 조회
    public List<Hashtag> getHashtagList() {
        return hashtagRepository.findAll();
    }


    /**
     * 해시태그 검색
     *
     * @param tag
     * @return videoList
     */
    public List<Video> searchHashtag(String tag){

        List<Video> videoList = new ArrayList<>();

        Optional<Hashtag> hashtag =  hashtagRepository.findByTitle(tag);

        if(hashtag.isPresent()){
            // 검색한 해시태그가 존재한다면, videoList 제작
            List<VideoHashtag> mapList = videoHashtagRepository.findAllByHashtagId(hashtag.get());

            for (VideoHashtag map : mapList){
                videoList.add(map.getVideoId());
            }

        } else {
            // 검색한 해시태그가 없다면 videoList는 null
            videoList = null;
        }

        return videoList;
    }


    /**
     * 해시태그 파싱 후 저장
     * - 동영상 업로드 할 때 사용
     *
     * @param tag
     * @param video
     * @return null
     */
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
                                .videoId(video)
                                .hashtagId(hashtag)
                                .build();

            videoHashtagRepository.save(map);
        }

    }

    //해시태그 삭제 - 테스트용
    public void delete(String title){
        Hashtag hashtag = hashtagRepository.findByTitle(title).get();

        //해시태그 db에서 삭제
        hashtagRepository.delete(hashtag);
    }

}
