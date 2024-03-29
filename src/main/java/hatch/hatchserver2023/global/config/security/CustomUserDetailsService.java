package hatch.hatchserver2023.global.config.security;

import hatch.hatchserver2023.domain.user.repository.UserRepository;
import hatch.hatchserver2023.global.common.response.code.UserStatusCode;
import hatch.hatchserver2023.global.common.response.exception.AuthException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

//@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder; //에러로 추가

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        if(uuid != null){
            return userRepository.findByUuid(UUID.fromString(uuid))
                    .orElseThrow(() -> new AuthException(UserStatusCode.UUID_NOT_FOUND));
        }else{
            throw new AuthException(UserStatusCode.UUID_IS_NULL); //TODO : 비회원???
        }
    }
}
