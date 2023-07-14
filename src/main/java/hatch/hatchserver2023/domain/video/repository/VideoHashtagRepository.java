package hatch.hatchserver2023.domain.video.repository;

import hatch.hatchserver2023.domain.video.domain.Hashtag;
import hatch.hatchserver2023.domain.video.domain.Video;
import hatch.hatchserver2023.domain.video.domain.VideoHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoHashtagRepository extends JpaRepository<VideoHashtag, Long> {

    public List<VideoHashtag> findAllByVideoId(Video videoId);
    public List<VideoHashtag> findAllByHashtagId(Hashtag hashtagId);
}
