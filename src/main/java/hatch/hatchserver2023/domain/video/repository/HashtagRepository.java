package hatch.hatchserver2023.domain.video.repository;

import hatch.hatchserver2023.domain.video.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    public Optional<Hashtag> findByTitle(String title);
}
