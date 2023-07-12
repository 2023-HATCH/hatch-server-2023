package hatch.hatchserver2023.domain.video.repository;

import hatch.hatchserver2023.domain.video.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findAllByVideoId(Video video);
}
