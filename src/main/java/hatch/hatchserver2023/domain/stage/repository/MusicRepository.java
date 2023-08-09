package hatch.hatchserver2023.domain.stage.repository;

import hatch.hatchserver2023.domain.stage.domain.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    public Music findByTitle(String title);

    public Music findByUuid(UUID uuid);

    // 전체 데이터 중 랜덤으로 정렬하여 1개만 가져옴
    @Query(value = "select * from music order by rand() limit 1", nativeQuery = true)
    List<Music> findRandomOne();

}
