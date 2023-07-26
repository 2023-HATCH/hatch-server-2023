package hatch.hatchserver2023.domain.user.application;

import hatch.hatchserver2023.domain.user.domain.User;
import hatch.hatchserver2023.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserUtilService {

    private final UserRepository userRepository;

    public UserUtilService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public List<User> getUsersById(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }
}
