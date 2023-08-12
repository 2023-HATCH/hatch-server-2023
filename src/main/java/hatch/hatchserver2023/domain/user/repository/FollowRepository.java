package hatch.hatchserver2023.domain.user.repository;

import hatch.hatchserver2023.domain.user.domain.Follow;
import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    public List<Follow> findAllByToUser(User toUser);
    public List<Follow> findAllByFromUser(User fromUser);
    public Optional<Follow> findByFromUserAndToUser(User fromUser, User toUser);
    // 팔로워 수
    public int countByToUser(User toUser);
    // 팔로잉 수
    public int countByFromUser(User fromUser);
}
