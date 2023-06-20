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
import java.util.*;
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
    @Column(nullable = false, unique = true)
    private Long id;

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type = "uuid-char")
    @Column(nullable = false, length = 36, unique = true) // uuid 값이 36자의 문자열로 저장됨
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


    // update methods
    public void updateLoginDefaultValues() {
        this.loginType = "kakao";
        this.followerCount = 0;
        this.followingCount = 0;
        this.roles = Collections.singletonList("ROLE_USER"); //회원가입된 사용자 기본 권한
    }

    // uuid prepersist (auto generate) + BaseTimeEntity prePersist
    @Override
    public void prePersist() {
        this.uuid = UUID.randomUUID();
        super.prePersist(); //BaseTimeEntity
    }


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


    // 얘네 이해 안됨.. 일단 false여서 막히는 것 같아서 다 true로 반환값 바꿈. 값이 어디서 생기는 거지??
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
