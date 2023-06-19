package hatch.hatchserver2023.domain.user.repository;

import hatch.hatchserver2023.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByKakaoAccountNumber(Long kakaoAccountNumber);
}
