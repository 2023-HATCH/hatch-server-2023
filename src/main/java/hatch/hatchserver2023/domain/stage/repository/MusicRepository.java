package hatch.hatchserver2023.domain.stage.repository;

import hatch.hatchserver2023.domain.stage.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    public Music findByTitle(String title);

}
