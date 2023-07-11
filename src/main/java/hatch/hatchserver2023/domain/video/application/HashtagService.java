package hatch.hatchserver2023.domain.video.application;

import hatch.hatchserver2023.domain.video.domain.Hashtag;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.repository.HashtagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    public HashtagService(HashtagRepository hashtagRepository){
        this.hashtagRepository = hashtagRepository;
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

        Hashtag hashtag =  hashtagRepository.findByTitle(tag)
                // 검색한 해시태그가 없다면 videoList를 null로 반환
                .orElse(Hashtag
                        .builder()
                        .videoList(null)
                        .build()
                );

        return hashtag.getVideoList();
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

            hashtag.getVideoList().add(video);
            hashtagRepository.save(hashtag);
        }

    }


}
