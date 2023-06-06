package hatch.hatchserver2023.domain.stage.repository;

import hatch.hatchserver2023.domain.stage.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MusicRepository extends JpaRepository<Music, UUID> {

    public Music findByTitle(String title);

}
