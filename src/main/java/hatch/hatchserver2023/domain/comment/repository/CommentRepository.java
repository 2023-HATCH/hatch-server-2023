package hatch.hatchserver2023.domain.comment.repository;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.comment.domain.Comment;
import hatch.hatchserver2023.domain.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    public List<Comment> findAllByVideoId(Video video);

    public List<Comment> findByVideoIdAndUserId(Video videoId, User userId);

    public Optional<Comment> findByUuid(UUID uuid);
}
