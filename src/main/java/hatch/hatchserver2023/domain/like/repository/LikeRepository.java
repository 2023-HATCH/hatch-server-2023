package hatch.hatchserver2023.domain.like.repository;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.like.domain.Like;
import hatch.hatchserver2023.domain.video.domain.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    //사용자의 좋아요 목록 찾기
    public Slice<Like> findAllByUserId(User user, Pageable pageable);

    //한 동영상의 좋아요 목록 찾기
    public List<Like> findAllByVideoId(Video video);

    //단 하나의 좋아요 찾기
    public Optional<Like> findByVideoIdAndUserId(Video video, User user);

    public Optional<Like> findByVideoIdAndUserId(long videoId, long userId);

    //한 동영상의 좋아요 개수 세기
    //TODO: 최적화 방법 고민
    public long countByVideoId(Video video);

}
