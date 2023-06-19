package hatch.hatchserver2023.domain.user.domain;

import hatch.hatchserver2023.global.common.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User extends BaseTimeEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36) // uuid 값이 36자의 문자열로 저장됨
    private UUID uuid;

    @Column(nullable = false, length = 10)
    private String nickname;  //카카오 필수 항목

    @Column(length = 30)
    private String email;  //카카오 선택 항목

    @Column(length = 30)
    private String twitterAccount;

    @Column(length = 30)
    private String instagramAccount;

    @Column(nullable = false, length = 10)
    private String loginType;

    @Column(nullable = false, length = 10)
    private Long kakaoAccountNumber;

    @Column(nullable = false)
    private int followingCount;

    @Column(nullable = false)
    private int followerCount;

    // TODO : 자기소개 글자수 제한?
    private String introduce;

    private String profileImg; //카카오 필수 항목

    // UserDetails 에 쓰기 위한 roles
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();



    //////////-- set user roles(Authentication) : implements UserDetails --//////////

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { //roles 필드를 SimpleGrantedAuthority객체로 바꾼 리스트 반환. 얘가 내부적으로 여기저기 쓰임
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.getUuid().toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
