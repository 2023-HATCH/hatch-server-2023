package hatch.hatchserver2023.domain.video.repository;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.video.domain.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    public Optional<Video> findByUuid(UUID uuid);

    public Slice<Video> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query(value = "SELECT * FROM video order by RAND()", nativeQuery = true)
    public Slice<Video> findAllOrderByRandom(Pageable pageable);

    public Slice<Video> findAllByOrderByCreatedAtDesc(Pageable pageable);

    public Slice<Video> findAllByOrderByLikeCountDesc(Pageable pageable);

    public Slice<Video> findAllByOrderByViewCountDesc(Pageable pageable);

    public Slice<Video> findAllByUser(User user, Pageable pageable);
}
