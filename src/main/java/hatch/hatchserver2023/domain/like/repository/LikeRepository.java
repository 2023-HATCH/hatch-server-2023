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
    public Slice<Like> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    //한 동영상의 좋아요 목록 찾기
    public List<Like> findAllByVideo(Video video);

    //단 하나의 좋아요 찾기
    public Optional<Like> findByVideoAndUser(Video video, User user);

    public Optional<Like> findByVideoAndUser(long videoId, long userId);

    //한 동영상의 좋아요 개수 세기
    public long countByVideo(Video video);

}
